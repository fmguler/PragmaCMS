/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.service.content.domain.Content;
import com.fmguler.ven.Criteria;
import com.fmguler.ven.Ven;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Handles content operations
 * Uses fmgVen for db operations (No DAO used for rapid prototyping)
 * @author Fatih Mehmet Güler
 */
public class ContentServiceImpl implements ContentService {
    private DataSource dataSource;
    private Ven ven;

    /**
     * Return the content with the specified path
     * @param path the path of the content
     * @return content object with path and data
     */
    public Content getContent(String path) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        criteria.eq("Content.path", path);
        List list = ven.list(Content.class, joins, criteria);
        if (list.isEmpty()) return null;
        return (Content)list.get(0);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        ven = new Ven();
        ven.setDataSource(dataSource);
        ven.addDomainPackage("com.fmguler.cms.service.content.domain");
        //ven.setDebug(true);
    }
}
