/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.controller;

import java.util.Date;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;

/**
 * Redirecting static resources to servlet container's default servlet
 *
 * @author Fatih Mehmet Güler
 */
@Controller
public class DefaultServletController implements ServletContextAware {
    public static final boolean STATIC_RESOURCE_CDN_ENABLED = false; //should be false while debugging
    private ServletContext servletContext;

    @RequestMapping
    protected ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();

        //required for jetty, which forwards jsps to controllers because of /* mapping in web.xml for dispatcher servlet
        if (path.endsWith(".jsp")) {
            RequestDispatcher dispatcher = servletContext.getNamedDispatcher("jsp"); //this is the trick
            dispatcher.forward(request, response);
            return null;
        }

        //we have to forward static resources to servlet container manually, otherwise content controller will receive it
        if (isStaticResource(path)) {
            
            //Poor man's CDN
            if (STATIC_RESOURCE_CDN_ENABLED){
                response.sendRedirect("http://static.pragmacms.com" + path);
                return null;
            } 
            
            //Load all static resources locally >>>
            response.addDateHeader("Expires", new Date().getTime() + 60L * 60 * 24 * 1000 * 30); //expires a month
            RequestDispatcher dispatcher = servletContext.getNamedDispatcher("default"); //this is the trick
            dispatcher.forward(request, response);
            return null;
        }

        System.out.println("Warning: DefaultServletController received a non jsp/static resource: " + path);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    //check if the given path is a static resource, e.g. js, css, image
    private boolean isStaticResource(String path) {
        return path.matches(".+\\.(js|css|gif|png|jpeg|jpg|ico|cur|html)");
    }

    //to get named dispatcher
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
