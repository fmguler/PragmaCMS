/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.content.domain;

import java.util.Date;

/**
 * Versions of template html
 * @author Fatih Mehmet Güler
 */
public class TemplateHistory {
    private Integer id;
    private Template template;
    private String html;
    private Date date;
    private String author;
    private String comment;

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
     * @return the html
     */
    public String getHtml() {
        return html;
    }

    /**
     * @param html the html to set
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
