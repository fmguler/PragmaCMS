/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.service.content.domain.*;
import com.fmguler.ven.Criteria;
import com.fmguler.ven.Ven;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Handles content operations
 * Uses fmgVen for db operations (No DAO used for rapid prototyping)
 * @author Fatih Mehmet Güler
 */
public class ContentServiceImpl implements ContentService {
    private NamedParameterJdbcTemplate template;
    private Ven ven;

    //PAGE----------------------------------------------------------------------
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
    public Page getPage(int id) {
        Set joins = new HashSet();
        joins.add("Page.template.templateAttributes");
        joins.add("Page.pageAttributes");
        Criteria criteria = new Criteria();
        criteria.eq("Page.id", id);

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
    public void updatePageRedirects(String oldRedirect, String newRedirect) {
        Map paramMap = new HashMap();
        paramMap.put("oldRedirect", oldRedirect);
        paramMap.put("newRedirect", newRedirect);

        String sql = "UPDATE page set new_path = :newRedirect where new_path = :oldRedirect";
        template.update(sql, paramMap);
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
    public void savePage(Page page) {
        ven.save(page);
    }

    @Override
    public void removePage(int id) {
        ven.delete(id, Page.class);
    }

    //TEMPLATE------------------------------------------------------------------
    @Override
    public Template getTemplate(int id) {
        Set joins = new HashSet();
        joins.add("Template.templateAttributes");
        return (Template)ven.get(id, Template.class, joins);
    }

    @Override
    public List getTemplates() {
        Set joins = new HashSet();
        joins.add("Template.templateAttributes");
        Criteria criteria = new Criteria();
        criteria.orderAsc("Template.name");
        List list = ven.list(Template.class, joins, criteria);
        return list;
    }

    //ATTRIBUTE-----------------------------------------------------------------
    @Override
    public PageAttribute getPageAttribute(int id) {
        Set joins = new HashSet();
        joins.add("PageAttribute.page.template");
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
    public void removePageAttribute(int id) {
        ven.delete(id, PageAttribute.class);
    }

    //ATTACHMENT----------------------------------------------------------------
    @Override
    public List getPageAttachments(int pageId) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        criteria.eq("PageAttachment.pageId", pageId);
        List list = ven.list(PageAttachment.class, joins, criteria);
        return list;
    }

    @Override
    public PageAttachment getPageAttachment(int id) {
        Set joins = new HashSet();
        joins.add("PageAttachment.page");
        return (PageAttachment)ven.get(id, PageAttachment.class, joins);
    }

    @Override
    public void savePageAttachment(PageAttachment pageAttachment) {
        ven.save(pageAttachment);
    }

    //AUTHOR--------------------------------------------------------------------
    public Author getAuthor(String username) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        criteria.eq("Author.username", username);
        List<Author> list = ven.list(Author.class, joins, criteria);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void setDataSource(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
        ven = new Ven();
        ven.setDataSource(dataSource);
        ven.addDomainPackage("com.fmguler.cms.service.content.domain");
        //ven.setDebug(true);
    }
}
