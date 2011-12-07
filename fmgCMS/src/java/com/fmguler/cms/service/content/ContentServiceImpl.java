/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.service.content.domain.Attribute;
import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.content.domain.Template;
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

    @Override
    public Page getPage(String path) {
        Set joins = new HashSet();
        joins.add("Page.template.templateAttributes.attribute");
        joins.add("Page.pageAttributes.attribute");
        Criteria criteria = new Criteria();
        criteria.eq("Page.path", path);
        List list = ven.list(Page.class, joins, criteria);
        if (list.isEmpty()) return null;
        return (Page)list.get(0);
    }

    @Override
    public List getPages() {
        Set joins = new HashSet();
        joins.add("Page.template.templateAttributes.attribute");
        joins.add("Page.pageAttributes.attribute");
        Criteria criteria = new Criteria();
        List list = ven.list(Page.class, joins, criteria);
        return list;
    }
    
    @Override
    public List getTemplates() {
        Set joins = new HashSet();
        joins.add("Template.templateAttributes.attribute");
        Criteria criteria = new Criteria();
        List list = ven.list(Template.class, joins, criteria);
        return list;
    }
    
    @Override
    public Attribute getAttribute(int id){
        return (Attribute)ven.get(id, Attribute.class, new HashSet());
    }
    
    @Override
    public void saveAttribute(Attribute attribute){
        ven.save(attribute);
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
