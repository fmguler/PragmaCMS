/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.content.domain;

import java.util.Date;

/**
 * Binary attachments of a page
 * @author Fatih Mehmet Güler
 */
public class PageAttachment {
    private Integer id;
    private Page page;
    private String name;
    private String contentKey;
    private String contentType = "application/octet-stream";
    private Integer contentLength;
    private Date lastModified = new Date();

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
     * @return the page
     */
    public Page getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(Page page) {
        this.page = page;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the contentKey
     */
    public String getContentKey() {
        return contentKey;
    }

    /**
     * @param contentKey the contentKey to set
     */
    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return the contentLength
     */
    public Integer getContentLength() {
        return contentLength;
    }

    /**
     * @param contentLength the contentLength to set
     */
    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
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
}
