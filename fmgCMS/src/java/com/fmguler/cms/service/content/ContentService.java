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
     * Save the template attribute
     */
    void saveTemplateAttribute(TemplateAttribute templateAttribute);

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
}
