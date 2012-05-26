    /*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.domain.Author;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Intercepts requrest and checks authentication & authorization
 * @author Fatih Mehmet Güler
 */
public class AAInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //set the locale (too lazy to create separate interceptor just for this)
        request.setAttribute("locale", RequestContextUtils.getLocale(request));

        //login pages
        String path = extractPath(request);
        if (!path.endsWith("/edit") && !path.startsWith("/admin")) return true;
        if (path.equals("/admin/login")) return true;
        if (path.equals("/admin/login.jsp")) return true;
        //we also protect static admin files, but these are required for login
        if (path.equals("/admin/js/bootstrap/css/bootstrap.css")) return true;
        if (path.equals("/admin/js/star.png")) return true;

        //redirect to login, but continue to this url after login
        Author user = (Author)request.getSession().getAttribute("user");
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
