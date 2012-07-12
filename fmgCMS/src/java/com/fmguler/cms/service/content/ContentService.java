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
     * Return the page with the specified path
     * @param path the path of the page
     * @return page object with attributes
     */
    public Page getPage(String path);

    /**
     * @return all pages in the system
     */
    public List getPages();

    /**
     * @param id template id
     * @return the template with the specified id
     */
    Template getTemplate(int id);

    /**
     * @return all templates in the system
     */
    public List getTemplates();

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

    void updatePageRedirects(String oldRedirect, String newRedirect);

    /**
     * Get the author by username
     * @return Author in the system
     */
    Author getAuthor(String username);

    void removePage(int id);

    void savePageAttributeHistory(PageAttributeHistory pageAttributeHistory);

    /**
     * Get the previous versions of a page attribute
     * @param pageId the page id
     * @param attribute the attribute name
     * @return all versions of the attribute including current
     */
    List getPageAttributeHistories(Integer pageId, String attribute);

    PageAttributeHistory getPageAttributeHistory(int id);

    void removePageAttachment(int id);

    void saveTemplate(Template template);

    void removeTemplate(int id);

    List getTemplateHistories(Integer templateId);

    TemplateHistory getTemplateHistory(int id);

    void saveTemplateHistory(TemplateHistory templateHistory);

    Template getTemplate(String path);

    void removePageAttributeHistory(int id);

    void removeTemplateHistory(int id);

    List getPages(int templateId);
}
