/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.template;

import java.io.File;
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
     * Returns file info for the specified resource of the template
     * @param resourcePath the path of the resource contained in the template
     * @return file info, to be used for reading
     */
    File getResource(String resourcePath);

    /**
     * Returns the raw source of the template
     * @param templateName the template (file in the directory)
     * @return the contents of the template
     */
    String getTemplateSource(String templateName);

    /**
     * Merge template with model
     */
    String mergeFromSource(String templateSource, Map model);
}
