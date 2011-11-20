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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Serves all content, dynamic and static.
 * @author Fatih Mehmet Güler
 */
public class ContentController extends AbstractController {
    private ContentService contentService;
    private TemplateService templateService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //Based on the requested url, decide which page to be served
        //if the requested content is static, pipe it from template resources

        //get the requested path
        String path = extractPath(request);
        
        //set the default page for www.abc.com and www.abc.com/
        if (path.equals("/")) path = "/index.html";
        
        //if this webapp has a context e.g. fmgCMS and has no trailing slash, browser will send the subsequent static requests from the root, so we must redirect to fmgCMS/
        if (path.equals("")){
            response.sendRedirect("./");
            return null;
        }        

        //if the requested resource is static, pipe it from template service
        if (checkStaticResource(path)) {
            //TODO catch exception and return 404 if requested resource does not exist.
            templateService.pipeResource(path, response.getOutputStream());
            return null;
        }

        //get the page with attributes
        Page page = contentService.getPage(path);

        //return 404
        if (page == null) {
            response.sendError(404, "Page not found");
            return null;
        }

        //find the template, fill with attributes
        //use freemarker
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
    private boolean checkStaticResource(String path) {
        return path.matches(".+\\.(js|css|gif|png|jpeg|jpg)");
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
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
