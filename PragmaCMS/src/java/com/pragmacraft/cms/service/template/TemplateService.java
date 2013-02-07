/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.template;

import java.io.OutputStream;
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
    String merge(String templatePath, Map model) throws TemplateException;

    /**
     * Returns the raw source of the template
     * @param templatePath the template path (file in the directory)
     * @return the contents of the template
     */
    String getSource(String templatePath) throws TemplateException;

    /**
     * Merge template with model
     */
    String mergeFromSource(String templatePath, String templateSource, Map model) throws TemplateException;

    ;

    /**
     * Write template source (create/overwrite)
     * @param templatePath the template path
     * @return the os to write source
     * @throws TemplateException any error
     */
    OutputStream getSourceOutputStream(String templatePath) throws TemplateException;

    /**
     * Remove the template source (delete)
     * @param templatePath the template path
     */
    void removeSource(String templatePath);
}
