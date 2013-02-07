/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.template;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import java.io.*;
import java.util.Map;
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
    public String merge(String templatePath, Map model) throws TemplateException {
        try {
            StringWriter sw = new StringWriter();
            Template template = configuration.getTemplate(templatePath, "UTF-8");
            template.process(model, sw);
            return sw.toString();
        } catch (freemarker.template.TemplateException ex) {
            throw new TemplateException(TemplateException.ERROR_MERGE_FAILED, ex);
        } catch (IOException ex) {
            throw new TemplateException(TemplateException.ERROR_MERGE_FAILED, ex);
        }
    }

    @Override
    public String mergeFromSource(String templatePath, String templateSource, Map model) throws TemplateException {
        try {
            StringWriter sw = new StringWriter();
            Template template = new Template(templatePath, new StringReader(templateSource), new Configuration());
            template.process(model, sw);
            return sw.toString();
        } catch (freemarker.template.TemplateException ex) {
            throw new TemplateException(TemplateException.ERROR_MERGE_SOURCE_FAILED, ex);
        } catch (IOException ex) {
            throw new TemplateException(TemplateException.ERROR_MERGE_SOURCE_FAILED, ex);
        }
    }

    @Override
    public String getSource(String templatePath) throws TemplateException {
        try {
            File templateFile = new File(templateFolder, templatePath);
            return FileUtils.readFileToString(templateFile, "UTF-8");
        } catch (IOException ex) {
            throw new TemplateException(TemplateException.ERROR_READ_SOURCE_FAILED, ex);
        }
    }

    @Override
    public OutputStream getSourceOutputStream(String templatePath) throws TemplateException {
        try {
            File templateFile = new File(templateFolder, templatePath);
            return new FileOutputStream(templateFile);
        } catch (IOException ex) {
            throw new com.pragmacraft.cms.service.template.TemplateException(TemplateException.ERROR_WRITE_SOURCE_FAILED, ex);
        }
    }

    @Override
    public void removeSource(String templatePath){
            File templateFile = new File(templateFolder, templatePath);
            FileUtils.deleteQuietly(templateFile);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    //--------------------------------------------------------------------------
    public void setTemplateFolder(String templateFolder) {
        this.templateFolder = templateFolder;
    }
}
