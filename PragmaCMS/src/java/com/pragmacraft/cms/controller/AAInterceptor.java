    /*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.controller;

import com.pragmacraft.cms.service.account.domain.Author;
import com.pragmacraft.cms.service.content.ContentService;
import com.pragmacraft.cms.service.content.domain.Site;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Intercepts requrest and checks authentication & authorization
 *
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

        //get the path
        String path = extractPath(request);

        //if this is a 404 not found page of a nonexisting site, we should specify a nonexisting siteId so that content controller won't find a page and return nothing for 404
        if (siteId == null && path.toLowerCase().equals(ContentController.ERROR_404_PAGE)) siteId = 0;

        //this domain does not belong to any of the registered sites and there is no default web site (with domain '*')
        if (siteId == null) {
            Logger.getLogger(AAInterceptor.class.getName()).log(Level.WARNING, "This domain name does not match any site record, returning 404: {0} path: {1}", new Object[]{request.getServerName(), path});
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        //set the site as request attribute so that all controllers know which site is this current request belong to
        request.setAttribute("site", new Site(siteId));

        //if public page, put the site id to request and return 
        if (isPublicPage(path)) return true;

        //check if this user is logged in
        Author user = (Author)request.getSession().getAttribute("user");
        if (user == null) {
            //check if user is redirected from login page (has a valid authToken)
            user = AdminController.getUserByAuthToken(request.getParameter("authToken"));

            //either case remove authToken from the query string
            String queryStr = request.getQueryString();
            if (queryStr.contains("&authToken")) queryStr = queryStr.substring(0, request.getQueryString().lastIndexOf("&authToken"));

            //no authToken, redirect to login page
            if (user == null) {
                request.getSession().setAttribute("returnUrl", path + "?" + queryStr);
                response.sendRedirect(request.getContextPath() + "/admin/login");
                return false;
            }

            //if the user is authorized to this site add to session (consume authToken)
            if (user.getAccount().checkSite(siteId)) {
                request.getSession().setAttribute("user", user);
                Logger.getLogger(AAInterceptor.class.getName()).log(Level.INFO, "User: {0} consumed authentication token for site: {1}", new Object[]{user.getUsername(), request.getServerName()});

                //we could continue, but we want to remove authToken from query string
                response.sendRedirect(request.getContextPath() + path + "?" + queryStr);
                return false;
            }
        }

        //if the user enters /admin/ after login the AdminController cannot handle
        if (path.equals("/admin/")) {
            response.sendRedirect(request.getContextPath() + "/admin/pages");
            return false;
        }

        //security check for the logged in user.
        //NOTE: this does not happen since session cookie is domain based but, just in case...
        if (!user.getAccount().checkSite(siteId)) {
            Logger.getLogger(AAInterceptor.class.getName()).log(Level.WARNING, "The domain {0} does not belong to the account of this user userId: {1} username: {2}", new Object[]{request.getServerName(), user.getId(), user.getUsername()});
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); //fail silently do not invoke site 404.html           
            return false;
        }

        return true;
    }

    //check if this path is a public or protected page
    private boolean isPublicPage(String path) {
        //login pages
        if (!path.endsWith("/edit") && !(path.startsWith("/admin") || path.startsWith("/account"))) return true;
        if (path.equals("/admin/checkLogin")) return true;
        if (path.equals("/admin/login")) return true;
        if (path.equals("/admin/login.jsp")) return true;
        if (path.equals("/admin/signup")) return true;
        if (path.equals("/admin/signup.jsp")) return true;
        if (path.equals("/admin/doSignup")) return true;

        //we also protect static admin files, but these are required for login
        if (path.equals("/admin/js/star.png")) return true;
        if (path.equals("/admin/js/signup.js")) return true;
        if (path.equals("/admin/js/bootstrap/css/bootstrap.css")) return true;

        return false;
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
