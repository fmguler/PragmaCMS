/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.content.domain.PageAttribute;
import com.fmguler.cms.service.content.domain.TemplateAttribute;
import com.fmguler.cms.service.template.TemplateService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        //if the requested resource is static, pipe it from template service
        if (isStaticResource(path)) {
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

        //TODO: check page last modified date and return not modified (304)
        //page.getLastModifiedDate()

        //find the template, fill with attributes
        String templateName = page.getTemplate().getName();
        Map model = getPageAttributesMap(page);
        String pageHtml = templateService.merge(templateName, model);

        //write the page to the response
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getOutputStream().write(pageHtml.getBytes("UTF-8"));

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
        return path.matches(".+\\.(js|css|gif|png|jpeg|jpg|ico)");
    }

    //handle static resource, pipe from template resources, handle caching
    private void handleStaticResource(String resourcePath, HttpServletRequest request, HttpServletResponse response) {
        //TODO: catch template service exception and return 404 if requested resource does not exist.
        //NOTE: some of the following code block is taken from winstone code (StaticResourceServlet.java)
        try {
            File res = templateService.getResource(resourcePath);
            long cachedResDate = request.getDateHeader("If-Modified-Since");

            if (!res.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (res.isDirectory()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            //set the mime type
            String mimeType = servletContext.getMimeType(resourcePath.toLowerCase());
            if (mimeType != null) response.setContentType(mimeType);

            //checked last modified of the template resource
            if ((cachedResDate != -1) && (cachedResDate < (System.currentTimeMillis() / 1000L * 1000L)) && (cachedResDate >= (res.lastModified() / 1000L * 1000L))) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.setContentLength(0);
                response.flushBuffer();
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength((int)res.length());
                response.addDateHeader("Last-Modified", res.lastModified());
                IOUtils.copyLarge(new FileInputStream(res), response.getOutputStream());
            }
        } catch (IOException ex) {
            Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Cannot write to response output stream", ex);
        }
    }

    //get all the page and template attributes and return them as map
    private Map getPageAttributesMap(Page page) {
        Map result = new HashMap();

        Iterator it = page.getTemplate().getTemplateAttributes().iterator();
        while (it.hasNext()) {
            TemplateAttribute attribute = (TemplateAttribute)it.next();
            result.put(attribute.getAttribute().getAttribute(), attribute.getAttribute().getValue());
        }

        it = page.getPageAttributes().iterator();
        while (it.hasNext()) {
            PageAttribute attribute = (PageAttribute)it.next();
            result.put(attribute.getAttribute().getAttribute(), attribute.getAttribute().getValue());
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

    //to get named dispatcher
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}