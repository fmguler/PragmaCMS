/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Serves pages.
 * @author Fatih Mehmet Güler
 */
public class PageController extends MultiActionController {
    private ContentService contentService;

    //view page in a template
    public ModelAndView viewPage(HttpServletRequest request, HttpServletResponse response) {
        return new ModelAndView();
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
