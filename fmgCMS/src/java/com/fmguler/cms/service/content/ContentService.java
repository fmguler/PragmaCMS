/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.content;

import com.fmguler.cms.service.content.domain.*;
import java.util.List;

/**
 * Handles content operations
 * Uses fmgVen for db operations (No DAO used for rapid prototyping)
 * @author Fatih Mehmet Güler
 */
public interface ContentService {
    /**
     * Return the page with the specified path for the site
     * @param path the path of the page
     * @return page object with attributes
     */
    public Page getPage(String path, int siteId);

    /**
     * @return all pages in the system for the site
     */
    public List getPages(int siteId);

    /**
     * @param id template id
     * @return the template with the specified id
     */
    Template getTemplate(int id);

    /**
     * @return all templates in the system for the site
     */
    public List getTemplates(int siteId);

    /**
     * @param id the id of the page attribute
     * @return the page attribute
     */
    PageAttribute getPageAttribute(int id);

    /**
     * Save the page attribute
     */
    void savePageAttribute(PageAttribute pageAttribute);

    /**
     * Save the page
     */
    void savePage(Page page);

    /**
     * remove the attribute with the id
     */
    void removePageAttribute(int id);

    PageAttachment getPageAttachment(int id);

    void savePageAttachment(PageAttachment pageAttachment);

    List getPageAttachments(int pageId);

    Page getPage(int id);

    void updatePageRedirects(String oldRedirect, String newRedirect, int siteId);

    void removePage(int id);

    void savePageAttributeHistory(PageAttributeHistory pageAttributeHistory);

    /**
     * Get the previous versions of a page attribute
     * @param pageId the page id
     * @param attribute the attribute name
     * @return all versions of the attribute including current
     */
    List getPageAttributeHistories(Integer pageId, String attribute, int siteId);

    PageAttributeHistory getPageAttributeHistory(int id);

    void removePageAttachment(int id);

    void saveTemplate(Template template);

    void removeTemplate(int id);

    List getTemplateHistories(Integer templateId, int siteId);

    TemplateHistory getTemplateHistory(int id);

    void saveTemplateHistory(TemplateHistory templateHistory);

    Template getTemplate(String path, int siteId);

    void removePageAttributeHistory(int id);

    void removeTemplateHistory(int id);

    List getPages(int templateId, int siteId);

    List getSites();

    /**
     * Resolve the site id of the request by host header
     * @param domainName the host header - requested domain
     * @return the site id of this domain or null
     */
    Integer resolveSiteId(String domainName);

    Site getSite(int id);
}
