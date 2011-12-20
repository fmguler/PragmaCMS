/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 *
 * @author Fatih Mehmet Güler
 */
public class AAInterceptor extends HandlerInterceptorAdapter{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = extractPath(request);
        if (!path.endsWith("/edit") && !path.startsWith("/admin")) return true;
        

        String authorized = (String)request.getSession().getAttribute("authorized");
        if (authorized == null) {
            String authToken = request.getParameter("authToken");
            if (authToken == null){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                System.out.println("Authorization token is null");
                return false;
            }else{
                if (authToken.equals("qPoCeuZSUFyKxZPSBQq2")){
                    request.getSession().setAttribute("authorized", "true");
                }
            }
        }

        return true;
    }

    //get the path from request
    private String extractPath(HttpServletRequest request) {
        String path = "";
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath)) path = requestUri.substring(contextPath.length());
        else path = requestUri;
        return path;
    }
}
