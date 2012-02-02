/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.template;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Handles templating operations, using freemarker.
 * @author Fatih Mehmet Güler
 */
public class TemplateServiceImpl implements TemplateService {
    private String templateFolder;
    private Configuration configuration;

    /**
     * Initialize the service.
     */
    public void init() throws IOException {
        configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File(templateFolder));
        configuration.setObjectWrapper(new DefaultObjectWrapper());
    }

    @Override
    public String merge(String templateName, Map model) {
        try {
            StringWriter sw = new StringWriter();
            Template template = configuration.getTemplate(templateName, "UTF-8");
            template.process(model, sw);
            return sw.toString();
        } catch (TemplateException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot merge template", ex);
        } catch (IOException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot find template file", ex);
        }
        return "";
    }

    @Override
    public String mergeFromSource(String templateSource, Map model) {
        try {
            StringWriter sw = new StringWriter();
            Template template = new Template("template", new StringReader(templateSource), new Configuration());
            template.process(model, sw);
            return sw.toString();
        } catch (TemplateException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot merge template from source", ex);
        } catch (IOException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot merge template from source", ex);
        }
        return "";
    }

    @Override
    public String getTemplateSource(String templateName) {
        try {
            File templateFile = getResource(templateName);
            return FileUtils.readFileToString(templateFile, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot read template file", ex);
        }
        return "";
    }

    @Override
    public File getResource(String resourcePath) {
        File file = new File(templateFolder, resourcePath);
        return file;
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void setTemplateFolder(String templateFolder) {
        this.templateFolder = templateFolder;
    }
}
