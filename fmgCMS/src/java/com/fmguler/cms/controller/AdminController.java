/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
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

    public ModelAndView home(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        List pages = contentService.getPages();
        mv.addObject("pages", pages);
        return mv;
    }
    
    public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView();
        String pageUrl = ServletRequestUtils.getStringParameter(request, "page", "");
        Page page = contentService.getPage(pageUrl);
        mv.addObject("page", page);
        mv.addObject("pageUrl", pageUrl);
        return mv;
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
