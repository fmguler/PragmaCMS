/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.template;

import java.util.Map;

/**
 * Templating operations.
 * @author Fatih Mehmet Güler
 */
public interface TemplateService {
    /**
     * Merge a template with the given data
     * @param templatePath path of the template, relative to template folder
     * @param model the data, with attribute names and values
     * @return merged string
     */
    String merge(String templatePath, Map model);

    /**
     * Returns the raw source of the template
     * @param templatePath the template (file in the directory)
     * @return the contents of the template
     */
    String getSource(String templatePath);

    /**
     * Merge template with model
     */
    String mergeFromSource(String templatePath, String templateSource, Map model);
}
