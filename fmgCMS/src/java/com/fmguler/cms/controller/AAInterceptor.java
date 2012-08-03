    /*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.account.domain.Author;
import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Site;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Intercepts requrest and checks authentication & authorization
 * @author Fatih Mehmet Güler
 */
public class AAInterceptor extends HandlerInterceptorAdapter {
    private ContentService contentService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //set the locale (too lazy to create separate interceptor just for this)
        request.setAttribute("locale", RequestContextUtils.getLocale(request));

        //resolve the site id
        Integer siteId = contentService.resolveSiteId(request.getServerName());

        //this domain does not belong to any of the registered sites and there is no default web site (with domain '*')
        if (siteId == null) {
            Logger.getLogger(AAInterceptor.class.getName()).log(Level.WARNING, "This domain name does not match any site record, returning 404: {0}", request.getServerName());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        //set the site as request attribute so that all controllers know which site is this current request belong to
        request.setAttribute("site", new Site(siteId));

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

        //security check for the logged in user.
        //NOTE: this does not happen since session cookie is domain based but, just in case...
        if (!user.getAccount().checkSite(siteId)) {
            Logger.getLogger(AAInterceptor.class.getName()).log(Level.WARNING, "The domain {0} does not belong to the account of this user userId: {1}", new Object[]{request.getServerName(), user.getId()});
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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

    //SETTERS
    //--------------------------------------------------------------------------
    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
}
