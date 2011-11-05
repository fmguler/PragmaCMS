/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Page;
import java.io.IOException;
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
    public ModelAndView viewPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        String path = ServletRequestUtils.getStringParameter(request, "path", "");

        //get the page with attributes
        Page page = contentService.getPage(path);

        //return 404
        if (page == null) {
            response.sendError(404, "Page not found");
            return null;
        }
        
        //find the template, fill with attributes
        //use freemarker

        return null;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
