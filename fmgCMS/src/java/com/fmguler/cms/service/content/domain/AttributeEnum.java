/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content.domain;

/**
 * Attribute enumeration.
 * Used to track what attributes a template and its pages need
 * @author Fatih Mehmet Güler
 */
public class AttributeEnum {
    public static final int ATTRIBUTE_TYPE_TEMPLATE = 0;
    public static final int ATTRIBUTE_TYPE_PAGE = 1;
    private Integer id;
    private Template template;
    private String attributeName;
    private Integer attributeType = ATTRIBUTE_TYPE_PAGE;

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
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName the attributeName to set
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return the attributeType
     */
    public Integer getAttributeType() {
        return attributeType;
    }

    /**
     * @param attributeType the attributeType to set
     */
    public void setAttributeType(Integer attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public String toString() {
        return "attr: " + attributeName + " type: " + attributeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeEnum)) return super.equals(obj);
        AttributeEnum attr = (AttributeEnum)obj;
        return (attr.getAttributeName().equals(attributeName) && attr.getAttributeType().equals(attributeType));
    }
}
