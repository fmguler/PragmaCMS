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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * Handles templating operations, using freemarker.
 * @author Fatih Mehmet Güler
 */
public class TemplateServiceImpl implements TemplateService {
    private String templateFolder;
    Configuration configuration;

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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
            Template template = configuration.getTemplate(templateName, "UTF-8");
            template.process(model, osw);
            osw.flush();
            return new String(baos.toByteArray(), "UTF-8");
        } catch (TemplateException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot merge template", ex);
        } catch (IOException ex) {
            Logger.getLogger(TemplateServiceImpl.class.getName()).log(Level.SEVERE, "Cannot find template file", ex);
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
