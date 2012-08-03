/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.domain.Site;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Resolves custom web arguments
 *
 * @author Fatih Mehmet Güler
 */
public class CustomWebArgumentResolver implements WebArgumentResolver {
    public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
        Object result = UNRESOLVED;

        //for supported type: Site
        if (methodParameter.getParameterType().equals(Site.class)) {
            //get the site argument from request attribute (put in interceptor)
            if ((result = webRequest.getAttribute("site", NativeWebRequest.SCOPE_REQUEST)) == null) {
                throw new Exception("Cannot resolve Site argument, no site attribute in request");
            }
        }

        return result;
    }
}
