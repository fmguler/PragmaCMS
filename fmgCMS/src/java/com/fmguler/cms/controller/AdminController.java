/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Attribute;
import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.template.TemplateService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Privileged operations for admins.
 * @author Fatih Mehmet Güler
 */
@Controller
public class AdminController {
    private ContentService contentService;
    private TemplateService templateService;

    //admin home
    @RequestMapping()
    public String home(Model model) {
        List pages = contentService.getPages();
        List templates = contentService.getTemplates();
        model.addAttribute("pages", pages);
        model.addAttribute("templates", templates);
        return "admin/home";
    }

    //edit a page
    @RequestMapping("/**/edit")
    public String editPage(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        String path = extractEditPath(request);
        if (path.equals("")) path = "/index.html";
        Page page = contentService.getPage(path);
        
        //return 404
        if (page == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        model.addAttribute("page", page);
        model.addAttribute("path", path);
        return "admin/editPage";
    }
    
    //edit a template
    @RequestMapping()
    public String editTemplate(Model model) {
        //Not implemented
        return "admin/editPage";
    }

    //ajax - update page attribute
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String updateAttribute(@RequestParam String value, @RequestParam int id) {
        //update the attribute
        Attribute attribute = contentService.getAttribute(id);
        if (attribute == null) return null; //TODO: return error status
        attribute.setValue(value);
        contentService.saveAttribute(attribute);
        return "";
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //extract the path from requested url
    private String extractEditPath(HttpServletRequest request) {
        String path = "";
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath)) path = requestUri.substring(contextPath.length());
        else path = requestUri;
        //remove the trailing /edit
        path = path.substring(0, path.lastIndexOf("/edit"));
        return path;
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
}
