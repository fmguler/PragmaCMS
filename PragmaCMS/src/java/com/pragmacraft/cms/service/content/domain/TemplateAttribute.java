/*
 *  PragmaCMS
 *  Copyright 2013 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.content.domain;

/**
 * Scanned attributes of template, to hold default values and as enumeration
 *
 * @author Fatih Mehmet Güler
 */
public class TemplateAttribute {
    private Integer id;
    private Template template;
    private String attribute; //scanned attribute
    private String value; //default value
    private Boolean removed = false; 

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
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * @param attribute the attribute to set
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the removed
     */
    public Boolean getRemoved() {
        return removed;
    }

    /**
     * @param removed the removed to set
     */
    public void setRemoved(Boolean removed) {
        this.removed = removed;
    }

    
}
