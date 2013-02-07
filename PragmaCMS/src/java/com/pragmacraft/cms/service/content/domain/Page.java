/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.content.domain;

import com.fmguler.ven.util.VenList;
import java.util.Date;
import java.util.List;

/**
 * Represents each page with an accessible path in the system.
 * @author Fatih Mehmet Güler
 */
public class Page {
    private Integer id;
    private String path;
    private Template template;
    private Date lastModified;
    private String newPath;
    private List pageAttributes = new VenList(PageAttribute.class, "page");
    private Site site;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * @return the pageAttributes
     */
    public List getPageAttributes() {
        return pageAttributes;
    }

    /**
     * @param pageAttributes the pageAttributes to set
     */
    public void setPageAttributes(List pageAttributes) {
        this.pageAttributes = pageAttributes;
    }

    @Override
    public String toString() {
        return "Page: " + path + " attributes: {" + getPageAttributes() + "} " + template;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the newPath
     */
    public String getNewPath() {
        return newPath;
    }

    /**
     * @param newPath the newPath to set
     */
    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    /**
     * @return the site
     */
    public Site getSite() {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(Site site) {
        this.site = site;
    }

    /**
     * Check if this page belongs to this site
     */
    public boolean checkSite(Site siteToCheck) {
        if (siteToCheck == null || siteToCheck.getId() == null || site == null || site.getId() == null) return false;
        return site.getId().equals(siteToCheck.getId());
    }
}
