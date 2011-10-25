/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Content;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.ServletRequestUtils;
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
        ModelAndView mv = new ModelAndView();
        String path = ServletRequestUtils.getStringParameter(request, "path", "");

        Content content = contentService.getContent(path);
        if (content == null) return null; //TODO return 404

        mv.addObject("content", content);
        return mv;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
