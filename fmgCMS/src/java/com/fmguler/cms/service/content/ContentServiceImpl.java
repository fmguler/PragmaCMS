/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.content.domain.PageAttribute;
import com.fmguler.cms.service.content.domain.Template;
import com.fmguler.cms.service.content.domain.TemplateAttribute;
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
        joins.add("Page.template.templateAttributes");
        joins.add("Page.pageAttributes");
        Criteria criteria = new Criteria();
        criteria.eq("Page.path", path);
        
        //modify the query
        String query = ven.getQueryGenerator().generateSelectQuery(Page.class, joins);
        query += " left join (select page_id as __page_id, attribute as __attribute, max(version) as __max_version from page_attribute group by attribute, page_id) __pa on (__pa.__page_id = page_id and __pa.__attribute = page_page_attributes.attribute)";
        query += " left join (select template_id as __template_id, attribute as __attribute, max(version) as __max_version from template_attribute group by attribute, template_id) __ta on (__ta.__template_id = page.template_id and __ta.__attribute = page_template_template_attributes.attribute)";
        query += " where 1=1 ";
        query += " and (page_page_attributes.version is null or page_page_attributes.version = __pa.__max_version)";
        query += " and (page_template_template_attributes.version is null or page_template_template_attributes.version = __ta.__max_version) ";
        query += criteria.criteriaStringToSQL() + " and " + criteria.criteriaToSQL() + criteria.orderStringToSQL();

        //get the list
        List list = ven.getQueryMapper().list(query, criteria.getParameters(), Page.class);
        
        if (list.isEmpty()) return null;
        return (Page)list.get(0);
    }
    
    @Override
    public List getPages() {
        Set joins = new HashSet();
        joins.add("Page.template.templateAttributes");
        joins.add("Page.pageAttributes");
        Criteria criteria = new Criteria();
        criteria.orderAsc("Page.path");
        List list = ven.list(Page.class, joins, criteria);
        return list;
    }

    @Override
    public Template getTemplate(int id) {
        Set joins = new HashSet();
        joins.add("Template.templateAttributes");
        joins.add("Template.attributeEnumerations");
        return (Template)ven.get(id, Template.class, joins);
    }

    @Override
    public List getTemplates() {
        Set joins = new HashSet();
        joins.add("Template.templateAttributes");
        joins.add("Template.attributeEnumerations");
        Criteria criteria = new Criteria();
        criteria.orderAsc("Template.name");
        List list = ven.list(Template.class, joins, criteria);
        return list;
    }

    @Override
    public PageAttribute getPageAttribute(int id) {
        Set joins = new HashSet();
        joins.add("PageAttribute.page");
        return (PageAttribute)ven.get(id, PageAttribute.class, joins);
    }

    @Override
    public void savePageAttribute(PageAttribute pageAttribute) {
        ven.save(pageAttribute);
    }

    @Override
    public void saveTemplateAttribute(TemplateAttribute templateAttribute) {
        ven.save(templateAttribute);
    }

    @Override
    public void savePage(Page page) {
        ven.save(page);
    }

    @Override
    public void removePageAttribute(int id) {
        ven.delete(id, PageAttribute.class);
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
