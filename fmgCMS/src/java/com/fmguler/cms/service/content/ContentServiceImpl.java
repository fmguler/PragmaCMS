/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import javax.sql.DataSource;

/**
 *
 * @author Fatih Mehmet Güler
 */
public class ContentServiceImpl implements ContentService{
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
}
