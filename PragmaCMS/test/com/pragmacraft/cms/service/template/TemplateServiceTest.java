/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.template;

import com.pragmacraft.cms.service.template.TemplateException;
import com.pragmacraft.cms.service.template.TemplateService;
import com.pragmacraft.cms.service.template.TemplateServiceImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fatih Mehmet Güler
 */
public class TemplateServiceTest {
    private TemplateService templateService;

    //test merge
    public void testMerge() throws TemplateException {
        Map model = new HashMap();
        model.put("attr1", "value");
        System.out.println(templateService.merge("template1.html", model));
    }

    //build template service
    private static TemplateService buildTemplateService() throws IOException {
        TemplateServiceImpl templateService = new TemplateServiceImpl();
        templateService.setTemplateFolder("/projects/WebAppsHome/PragmaCMS/data/www/1");
        templateService.init();
        return templateService;
    }

    //test operations
    public static void main(String[] args) throws IOException, TemplateException {
        TemplateServiceTest test = new TemplateServiceTest();
        test.templateService = buildTemplateService();

        //test merge
        test.testMerge();
    }
}
