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
public class AAInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = extractPath(request);
        if (!path.endsWith("/edit") && !path.startsWith("/admin")) return true;
        if (path.equals("/admin/login")) return true;
        if (path.equals("/admin/login.jsp")) return true;

        //redirect to login, but continue to this url after login
        String user = (String)request.getSession().getAttribute("user");
        if (user == null) {
            request.getSession().setAttribute("returnUrl", path + "?" + request.getQueryString());
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return false;
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
