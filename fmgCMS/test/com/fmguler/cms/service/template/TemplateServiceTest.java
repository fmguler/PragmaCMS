/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.template;

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
        templateService.setTemplateFolder("D:/WebAppsHome/fmgCMS/www");
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
