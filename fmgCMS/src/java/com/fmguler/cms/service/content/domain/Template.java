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
    private String path;
    private List templateAttributes = new VenList(TemplateAttribute.class, "template");

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
        return "Template: " + name + " attributes: {" + getTemplateAttributes() + "}";
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

}
