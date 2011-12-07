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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Privileged operations for admins.
 * @author Fatih Mehmet Güler
 */
public class AdminController extends MultiActionController {
    private ContentService contentService;
    private TemplateService templateService;

    //admin home
    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        List pages = contentService.getPages();
        List templates = contentService.getTemplates();
        mv.addObject("pages", pages);
        mv.addObject("templates", templates);
        return mv;
    }
    
    //list page attributes
    public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        String path = ServletRequestUtils.getStringParameter(request, "path", "");
        Page page = contentService.getPage(path);
        mv.addObject("page", page);
        mv.addObject("path", path);
        return mv;
    }
    
    //update page attributes
    public ModelAndView updateAttribute(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        int attributeId = ServletRequestUtils.getIntParameter(request, "id", 0);
        String attributeValue = ServletRequestUtils.getStringParameter(request, "value", "");

        //update the attribute
        Attribute attribute = contentService.getAttribute(attributeId);
        if (attribute == null) return null; //TODO: return error status
        attribute.setValue(attributeValue);
        contentService.saveAttribute(attribute);
        
        return null;
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
