/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Redirecting static resources to servlet container's default servlet
 * @author Fatih Mehmet Güler
 */
public class DefaultServletController extends AbstractController {
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();

        //required for jetty, which forwards jsps to controllers because of /* mapping in web.xml for dispatcher servlet
        if (path.endsWith(".jsp")) {
            RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("jsp"); //this is the trick
            dispatcher.forward(request, response);
            return null;
        }

        //we have to forward static resources to servlet container manually, otherwise content controller will receive it
        if (isStaticResource(path)) {
            RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default"); //this is the trick
            dispatcher.forward(request, response);
            return null;
        }

        System.out.println("Warning: DefaultServletController received a non jsp/static resource: " + path);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    //check if the given path is a static resource, e.g. js, css, image
    private boolean isStaticResource(String path) {
        return path.matches(".+\\.(js|css|gif|png|jpeg|jpg)");
    }
}
