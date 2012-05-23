/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.content.domain.PageAttachment;
import com.fmguler.cms.service.content.domain.PageAttribute;
import com.fmguler.cms.service.content.domain.TemplateAttribute;
import com.fmguler.cms.service.resource.ResourceException;
import com.fmguler.cms.service.resource.ResourceService;
import com.fmguler.cms.service.resource.domain.Resource;
import com.fmguler.cms.service.template.TemplateService;
import com.fmguler.common.service.storage.StorageException;
import com.fmguler.common.service.storage.StorageService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

/**
 * Serves all content, dynamic and static.
 * @author Fatih Mehmet Güler
 */
@Controller
public class ContentController implements ServletContextAware {
    private ContentService contentService;
    private TemplateService templateService;
    private ResourceService resourceService;
    private StorageService storageService;
    private ServletContext servletContext;

    @RequestMapping
    protected ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //Based on the requested url, decide which page to be served
        //if the requested content is static, pipe it from template resources

        //get the requested path
        String path = extractPath(request);

        //set the default page for www.abc.com and www.abc.com/
        if (path.equals("/")) path = "/index.html";

        //if this webapp has a context e.g. fmgCMS and has no trailing slash, browser will send the subsequent static requests from the root, so we must redirect to fmgCMS/
        if (path.equals("")) {
            response.sendRedirect("./");
            return null;
        }

        //if the requested resource is page attachment (binary user uploaded content), download it from storage service
        Integer attachmentId;
        if ((attachmentId = checkPageAttachment(path)) != null) {
            handlePageAttachment(attachmentId, request, response);
            return null;
        }

        //if the requested resource is static, pipe it from template service
        //we decide if this is a static resource based on extension
        //since htm or html extension can be given to generated pages, they cannot be accessed directly
        //they can be retrieved as static file by appending ?preview (or any static file not having extension specified in isStaticResource)
        if (isStaticResource(path) || request.getParameter("preview") != null) {
            handleStaticResource(path, request, response);
            return null;
        }

        //get the page with attributes
        Page page = contentService.getPage(path);

        //return 404
        if (page == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        //if this page is renamed return 301 (permanent redirect)
        if (page.getNewPath() != null) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            StringBuffer absoluteUrl = request.getRequestURL();
            absoluteUrl.replace(absoluteUrl.length() - path.length(), absoluteUrl.length(), page.getNewPath());

            response.setHeader("Location", response.encodeRedirectURL(absoluteUrl.toString()));
            return null;
        }

        //get request last modified header to check not modified
        long cachedResDate = request.getDateHeader("If-Modified-Since");

        //TODO: if the assigned path to the page and it's template path are in different folders like /some-page and /template/page.html
        //it's resources will use /template as relative path, and return not found. We should implement pages like /some-page/ and look for resources starting with /some-page in /template

        //find the template, fill with attributes
        String templateName = page.getTemplate().getName();
        Map model = getPageAttributesMap(page);
        String pageHtml = "";

        if (request.getParameter("edit") != null && request.getSession().getAttribute("user") != null) {
            //inject editor code if this is an edit
            String templateSource = templateService.getSource(templateName);
            templateSource = AdminController.injectEditor(templateSource);
            pageHtml = templateService.mergeFromSource(templateName, templateSource, model);
        } else if ((cachedResDate != -1) && (cachedResDate < (System.currentTimeMillis() / 1000L * 1000L)) && (cachedResDate >= (page.getLastModified().getTime() / 1000L * 1000L))) {
            //not modified (browser cache)
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            response.setContentLength(0);
            response.flushBuffer();
            return null;
        } else {
            //regular merge
            pageHtml = templateService.merge(templateName, model);
        }

        //write the page to the response
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.addDateHeader("Last-Modified", page.getLastModified().getTime());
        byte[] content = pageHtml.getBytes("UTF-8");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);

        //no view
        return null;
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //extract the path from requested url
    private String extractPath(HttpServletRequest request) {
        String path = "";
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath)) path = requestUri.substring(contextPath.length());
        else path = requestUri;
        return path;
    }

    //check if the given path is a static resource, e.g. js, css, image
    private boolean isStaticResource(String path) {
        return path.matches(".+\\.(js|css|gif|png|jpeg|jpg|ico|swf|wmv|pdf|txt|xml)");
    }

    //handle static resource, pipe from template resources, handle caching
    private void handleStaticResource(String resourcePath, HttpServletRequest request, HttpServletResponse response) {
        //NOTE: some of the following code block is taken from winstone code (StaticResourceServlet.java)
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Resource resource = resourceService.getResource(resourcePath);
            long cachedResDate = request.getDateHeader("If-Modified-Since");

            //check not exists
            if (resource == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            //set the mime type
            String mimeType = servletContext.getMimeType(resourcePath.toLowerCase());
            if (mimeType != null) response.setContentType(mimeType);

            //checked last modified of the template resource
            if ((cachedResDate != -1) && (cachedResDate < (System.currentTimeMillis() / 1000L * 1000L)) && (cachedResDate >= (resource.getLastModified().getTime() / 1000L * 1000L))) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setContentLength(0);
                response.flushBuffer();
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength(resource.getContentLength());
                response.addDateHeader("Last-Modified", resource.getLastModified().getTime());
                inputStream = resourceService.getInputStream(resourcePath);
                outputStream = response.getOutputStream();
                IOUtils.copyLarge(inputStream, outputStream);
            }
        } catch (ResourceException ex) {
            Logger.getLogger(ContentController.class.getName()).log(Level.WARNING, "ResourceException while handling static resource", ex);
        } catch (IOException ex) {
            Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Cannot write static resource to response output stream", ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    //check if the given path is a page content attachment, e.g. js, css, image uploaded by user
    private Integer checkPageAttachment(String path) {
        Matcher matcher = Pattern.compile("^/page-attachment/([0-9]+).*").matcher(path);
        if (matcher.matches()) return Integer.valueOf(matcher.group(1));
        return null;
    }

    //handle static resource, pipe from template resources, handle caching
    private void handlePageAttachment(Integer attachmentId, HttpServletRequest request, HttpServletResponse response) {
        //TODO: catch template service exception and return 404 if requested resource does not exist.
        //NOTE: some of the following code block is taken from winstone code (StaticResourceServlet.java)
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            PageAttachment pageAttachment = contentService.getPageAttachment(attachmentId);
            long cachedResDate = request.getDateHeader("If-Modified-Since");

            if (pageAttachment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            //set the mime type
            String mimeType = pageAttachment.getContentType(); //servletContext.getMimeType(pageAttachment.getName());
            if (mimeType != null) response.setContentType(mimeType);

            //checked last modified of the template resource
            if ((cachedResDate != -1) && (cachedResDate < (System.currentTimeMillis() / 1000L * 1000L)) && (cachedResDate >= (pageAttachment.getLastModified().getTime() / 1000L * 1000L))) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setContentLength(0);
                response.flushBuffer();
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength(pageAttachment.getContentLength());
                response.addDateHeader("Last-Modified", pageAttachment.getLastModified().getTime());
                inputStream = storageService.getInputStream(pageAttachment.getContentKey());
                outputStream = response.getOutputStream();
                IOUtils.copyLarge(inputStream, outputStream);
            }
        } catch (StorageException ex) {
            Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Storage service error at handlePageAttachment", ex);
        } catch (IOException ex) {
            Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Cannot write to response output stream", ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    //get all the page and template attributes and return them as map
    private Map getPageAttributesMap(Page page) {
        Map result = new HashMap();

        Iterator it = page.getTemplate().getTemplateAttributes().iterator();
        while (it.hasNext()) {
            TemplateAttribute attribute = (TemplateAttribute)it.next();
            result.put(attribute.getAttribute(), attribute.getValue());
        }

        it = page.getPageAttributes().iterator();
        while (it.hasNext()) {
            PageAttribute attribute = (PageAttribute)it.next();
            result.put(attribute.getAttribute(), attribute.getValue());
        }

        return result;
    }

    //SETTERS
    //--------------------------------------------------------------------------
    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Autowired
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Autowired
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    //to get named dispatcher
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
