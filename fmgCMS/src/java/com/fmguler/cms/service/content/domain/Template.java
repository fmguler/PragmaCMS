/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.service.content.domain;

import com.fmguler.ven.util.VenList;
import java.util.List;

/**
 * Represents each template in the system.
 * Every page has a template.
 * @author Fatih Mehmet Güler
 */
public class Template {
    private Integer id;
    private String name;
    private List templateAttributes = new VenList(TemplateAttribute.class, "template");
    private List attributeEnumerations = new VenList(AttributeEnum.class, "template");

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
     * @return the templateAttributes
     */
    public List getTemplateAttributes() {
        return templateAttributes;
    }

    /**
     * @param templateAttributes the templateAttributes to set
     */
    public void setTemplateAttributes(List templateAttributes) {
        this.templateAttributes = templateAttributes;
    }

    @Override
    public String toString() {
        return "Template: " + name + " enumerations: {" + getAttributeEnumerations() + "} attributes: {" + getTemplateAttributes() + "}";
    }

    /**
     * @return the attributeEnumerations
     */
    public List getAttributeEnumerations() {
        return attributeEnumerations;
    }

    /**
     * @param attributeEnumerations the attributeEnumerations to set
     */
    public void setAttributeEnumerations(List attributeEnumerations) {
        this.attributeEnumerations = attributeEnumerations;
    }
}
