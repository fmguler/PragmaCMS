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
import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Handles templating operations, using freemarker.
 * <p>
 * Basically responsible for;
 * <li>Merge some template (a text file with placeholders) with a model
 * <li>Return resources relative to template folder
 *
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
    public String merge(String templatePath, Map model) {
        try {
            StringWriter sw = new StringWriter();
            Template template = configuration.getTemplate(templatePath, "UTF-8");
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
    public String mergeFromSource(String templatePath, String templateSource, Map model) {
        try {
            StringWriter sw = new StringWriter();
            Template template = new Template(templatePath, new StringReader(templateSource), new Configuration());
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
    public String getSource(String templatePath) {
        try {
            File templateFile = new File(templateFolder, templatePath);
            return FileUtils.readFileToString(templateFile, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot read template file", ex);
        }
        return "";
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void setTemplateFolder(String templateFolder) {
        this.templateFolder = templateFolder;
    }
}
