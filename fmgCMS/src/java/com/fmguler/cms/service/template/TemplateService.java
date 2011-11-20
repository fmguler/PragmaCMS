/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.template;

import java.io.OutputStream;
import java.util.Map;

/**
 * Templating operations.
 * @author Fatih Mehmet Güler
 */
public interface TemplateService {
    
    /**
     * Merge a template with the given data
     * @param templateName name of the template, in template folder
     * @param model the data, with attribute names and values
     * @return merged string
     */
    String merge(String templateName, Map model);

    /**
     * Pipes other resources in the template folder, according to resourcePath.
     * <p>
     * Used for reading resources referenced in merged templates.
     * @param resourcePath the path of the requested resource e.g. js/abc.js
     * @param os the outputstream to be written
     */
    void pipeResource(String resourcePath, OutputStream os);
}
