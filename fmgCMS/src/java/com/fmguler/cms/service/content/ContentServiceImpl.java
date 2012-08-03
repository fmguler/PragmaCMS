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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private Map<String, Integer> domainToSiteMap; //domain name to site map

    //create the domain to site map on init
    public void init() {
        List<Site> sites = getSites();
        domainToSiteMap = new HashMap(sites.size() * 2);

        //populate the map
        for (Site site : sites) {
            String[] domains = site.getDomains().split("\\s+"); //white space separated domain name regexes
            for (int i = 0; i < domains.length; i++) {
                Integer existingMapping = domainToSiteMap.get(domains[i]);
                if (existingMapping != null && !existingMapping.equals(site.getId())) {
                    Logger.getLogger(ContentServiceImpl.class.getName()).log(Level.SEVERE, "***WARNING*** same domain maps to different site: domain: {0} existing mapping: {1} illegal mapping: {2}", new Object[]{domains[i], existingMapping, site.getId()});
                    continue;
                }
                if (existingMapping != null && existingMapping.equals(site.getId())) continue; //duplicate domain in the same site
                //register the domain to site id
                domainToSiteMap.put(domains[i], site.getId());
            }
        }
    }

    //--------------------------------------------------------------------------
    //PAGE
    //--------------------------------------------------------------------------
    @Override
    public Page getPage(String path, int siteId) {
        Set joins = new HashSet();
        joins.add("Page.template");
        joins.add("Page.pageAttributes");
        Criteria criteria = new Criteria();
        criteria.eq("Page.path", path);
        criteria.eq("Page.siteId", siteId);
        criteria.and();

        //get the list
        List list = ven.list(Page.class, joins, criteria);

        if (list.isEmpty()) return null;
        return (Page)list.get(0);
    }

    @Override
    public Page getPage(int id) {
        Set joins = new HashSet();
        joins.add("Page.template");
        joins.add("Page.pageAttributes");
        joins.add("Page.site");
        Criteria criteria = new Criteria();
        return (Page)ven.get(id, Page.class, joins, criteria);
    }

    @Override
    public void updatePageRedirects(String oldRedirect, String newRedirect, int siteId) {
        Map paramMap = new HashMap();
        paramMap.put("oldRedirect", oldRedirect);
        paramMap.put("newRedirect", newRedirect);
        paramMap.put("siteId", siteId);

        String sql = "UPDATE page set new_path = :newRedirect where new_path = :oldRedirect and site_id = :siteId";
        template.update(sql, paramMap);
    }

    @Override
    public List getPages(int siteId) {
        Set joins = new HashSet();
        joins.add("Page.template");
        joins.add("Page.site");
        Criteria criteria = new Criteria();
        criteria.eq("Page.siteId", siteId);
        criteria.orderAsc("Page.path");
        List list = ven.list(Page.class, joins, criteria);
        return list;
    }

    @Override
    public List getPages(int templateId, int siteId) {
        Set joins = new HashSet();
        joins.add("Page.template");
        Criteria criteria = new Criteria();
        criteria.eq("Page.template.id", templateId);
        criteria.eq("Page.siteId", siteId);
        criteria.and();
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

    //--------------------------------------------------------------------------
    //TEMPLATE
    //--------------------------------------------------------------------------
    @Override
    public Template getTemplate(int id) {
        Set joins = new HashSet();
        joins.add("Template.site");
        return (Template)ven.get(id, Template.class, joins);
    }

    @Override
    public Template getTemplate(String path, int siteId) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        criteria.eq("Template.path", path);
        criteria.eq("Template.siteId", siteId).and();

        //get the list
        List list = ven.list(Template.class, joins, criteria);

        if (list.isEmpty()) return null;
        return (Template)list.get(0);
    }

    @Override
    public List getTemplates(int siteId) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        criteria.eq("Template.siteId", siteId);
        criteria.orderAsc("Template.name");
        List list = ven.list(Template.class, joins, criteria);
        return list;
    }

    @Override
    public TemplateHistory getTemplateHistory(int id) {
        Set joins = new HashSet();
        joins.add("TemplateHistory.template");
        return (TemplateHistory)ven.get(id, TemplateHistory.class, joins);
    }

    @Override
    public List getTemplateHistories(Integer templateId, int siteId) {
        Set joins = new HashSet();
        joins.add("TemplateHistory.template");
        Criteria criteria = new Criteria();
        criteria.eq("TemplateHistory.template.id", templateId);
        criteria.eq("TemplateHistory.template.siteId", siteId);
        criteria.orderDesc("TemplateHistory.date");
        return ven.list(TemplateHistory.class, joins, criteria);
    }

    @Override
    public void saveTemplate(Template template) {
        ven.save(template);
    }

    @Override
    public void saveTemplateHistory(TemplateHistory templateHistory) {
        ven.save(templateHistory);
    }

    @Override
    public void removeTemplate(int id) {
        ven.delete(id, Template.class);
    }

    @Override
    public void removeTemplateHistory(int id) {
        ven.delete(id, TemplateHistory.class);
    }

    //--------------------------------------------------------------------------
    //ATTRIBUTE
    //--------------------------------------------------------------------------
    @Override
    public PageAttribute getPageAttribute(int id) {
        Set joins = new HashSet();
        joins.add("PageAttribute.page.template");
        joins.add("PageAttribute.page.site");
        return (PageAttribute)ven.get(id, PageAttribute.class, joins);
    }

    @Override
    public PageAttributeHistory getPageAttributeHistory(int id) {
        Set joins = new HashSet();
        joins.add("PageAttributeHistory.page.site");
        return (PageAttributeHistory)ven.get(id, PageAttributeHistory.class, joins);
    }

    @Override
    public List getPageAttributeHistories(Integer pageId, String attribute, int siteId) {
        Set joins = new HashSet();
        joins.add("PageAttributeHistory.page");
        Criteria criteria = new Criteria();
        criteria.eq("PageAttributeHistory.page.id", pageId);
        criteria.eq("PageAttributeHistory.attribute", attribute).and();
        criteria.eq("PageAttributeHistory.page.siteId", siteId).and();
        criteria.orderDesc("PageAttributeHistory.date");
        return ven.list(PageAttributeHistory.class, joins, criteria);
    }

    @Override
    public void savePageAttribute(PageAttribute pageAttribute) {
        ven.save(pageAttribute);
    }

    @Override
    public void savePageAttributeHistory(PageAttributeHistory pageAttributeHistory) {
        ven.save(pageAttributeHistory);
    }

    @Override
    public void removePageAttribute(int id) {
        ven.delete(id, PageAttribute.class);
    }

    @Override
    public void removePageAttributeHistory(int id) {
        ven.delete(id, PageAttributeHistory.class);
    }

    //--------------------------------------------------------------------------
    //ATTACHMENT
    //--------------------------------------------------------------------------
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
        joins.add("PageAttachment.page.site");
        return (PageAttachment)ven.get(id, PageAttachment.class, joins);
    }

    @Override
    public void savePageAttachment(PageAttachment pageAttachment) {
        ven.save(pageAttachment);
    }

    @Override
    public void removePageAttachment(int id) {
        ven.delete(id, PageAttachment.class);
    }

    //--------------------------------------------------------------------------
    //AUTHOR
    //--------------------------------------------------------------------------
    public Author getAuthor(String username, int siteId) {
        Set joins = new HashSet();
        joins.add("Author.site");
        Criteria criteria = new Criteria();
        criteria.eq("Author.username", username);
        criteria.eq("Author.siteId", siteId);
        criteria.and();
        List<Author> list = ven.list(Author.class, joins, criteria);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    //--------------------------------------------------------------------------
    //SITE
    //--------------------------------------------------------------------------
    /**
     * Resolve the site id of the request by host header
     * @param domainName the host header - requested domain
     * @return the site id of this domain or null
     */
    public Integer resolveSiteId(String domainName) {
        Integer siteId = domainToSiteMap.get(domainName);
        if (siteId == null) siteId = domainToSiteMap.get("*"); //use the default web site (if exists)
        return siteId;
    }

    @Override
    public Site getSite(int id) {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        return (Site)ven.get(id, Site.class, joins, criteria);
    }

    @Override
    public List getSites() {
        Set joins = new HashSet();
        Criteria criteria = new Criteria();
        List list = ven.list(Site.class, joins, criteria);
        return list;
    }
    //--------------------------------------------------------------------------
    //SETTERS
    //--------------------------------------------------------------------------

    public void setDataSource(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
        ven = new Ven();
        ven.setDataSource(dataSource);
        ven.addDomainPackage("com.fmguler.cms.service.content.domain");
        //ven.setDebug(true);
    }
}
