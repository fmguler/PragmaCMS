/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.db.LiquibaseUtil;
import com.fmguler.cms.service.content.domain.Page;

/**
 * Test Content Service operations
 * @author Fatih Mehmet Güler
 */
public class ContentServiceTest {
    private ContentService contentService;

    //test get page
    public void testGetPage() {
        Page page = contentService.getPage("/index.html");
        System.out.println(page);
    }

    //build content service
    private static ContentService buildContentService() {
        ContentServiceImpl contentService = new ContentServiceImpl();
        contentService.setDataSource(LiquibaseUtil.getDataSource("fmgcmsdb"));
        return contentService;
    }

    //test operations
    public static void main(String[] args) {
        ContentServiceTest test = new ContentServiceTest();
        test.contentService = buildContentService();

        //test Get Page
        test.testGetPage();
    }
}
