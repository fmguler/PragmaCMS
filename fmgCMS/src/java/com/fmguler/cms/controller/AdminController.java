/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.Attribute;
import com.fmguler.cms.service.content.domain.AttributeEnum;
import com.fmguler.cms.service.content.domain.Page;
import com.fmguler.cms.service.content.domain.PageAttribute;
import com.fmguler.cms.service.content.domain.Template;
import com.fmguler.cms.service.content.domain.TemplateAttribute;
import com.fmguler.cms.service.template.TemplateService;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Privileged operations for admins.
 * @author Fatih Mehmet Güler
 */
@Controller
public class AdminController {
    private ContentService contentService;
    private TemplateService templateService;

    //admin home
    @RequestMapping()
    public String home(Model model) {
        List pages = contentService.getPages();
        List templates = contentService.getTemplates();
        model.addAttribute("pages", pages);
        model.addAttribute("templates", templates);
        return "admin/home";
    }

    //edit a page
    @RequestMapping("/**/edit")
    public String editPage(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        String path = extractEditPath(request);
        if (path.equals("")) return "redirect:/index.html/edit";
        Page page = contentService.getPage(path);

        //creating a new page
        if (page == null) page = new Page();

        //calculate missing page and template attributes
        List missingPageAttributes = new LinkedList();
        List missingTemplateAttributes = new LinkedList();
        calculateMissingAttributes(page, missingPageAttributes, missingTemplateAttributes);

        model.addAttribute("page", page);
        model.addAttribute("path", path);
        model.addAttribute("missingPageAttributes", missingPageAttributes);
        model.addAttribute("missingTemplateAttributes", missingTemplateAttributes);
        model.addAttribute("templates", contentService.getTemplates());
        return "admin/editPage";
    }

    @RequestMapping
    @ResponseBody
    public String savePage(Page page) {
        contentService.savePage(page);
        return "";
    }

    //edit a template
    @RequestMapping()
    public String editTemplate(@RequestParam int id, Model model) {
        Template template = contentService.getTemplate(id);

        //return 404
        if (template == null) {
            return null;
        }

        //calculate missing page and template attributes
        List missingTemplateAttributes = new LinkedList();
        calculateMissingAttributes(template, missingTemplateAttributes);

        model.addAttribute("template", template);
        model.addAttribute("missingTemplateAttributes", missingTemplateAttributes);
        return "admin/editTemplate";
    }

    //ajax - add page attribute
    @RequestMapping()
    @ResponseBody
    public String addPageAttribute(@RequestParam String attributeName, @RequestParam Integer pageId) {
        PageAttribute pageAttribute = new PageAttribute();
        Page page = new Page();
        page.setId(pageId);
        pageAttribute.setPage(page);
        Attribute attribute = new Attribute();
        attribute.setAttribute(attributeName);
        attribute.setValue(""); //freemarker does not like nulls
        contentService.saveAttribute(attribute);
        pageAttribute.setAttribute(attribute);
        contentService.savePageAttribute(pageAttribute);
        return "";
    }

    //ajax - add template attribute
    @RequestMapping()
    @ResponseBody
    public String addTemplateAttribute(@RequestParam String attributeName, @RequestParam Integer templateId) {
        TemplateAttribute templateAttribute = new TemplateAttribute();
        Template template = new Template();
        template.setId(templateId);
        templateAttribute.setTemplate(template);
        Attribute attribute = new Attribute();
        attribute.setAttribute(attributeName);
        attribute.setValue(""); //freemarker does not like nulls
        contentService.saveAttribute(attribute);
        templateAttribute.setAttribute(attribute);
        contentService.saveTemplateAttribute(templateAttribute);
        return "";
    }

    //ajax - update page attribute
    @RequestMapping()
    @ResponseBody
    public String updateAttribute(@RequestParam String value, @RequestParam int id) {
        //update the attribute
        Attribute attribute = contentService.getAttribute(id);
        if (attribute == null) return null; //TODO: return error status
        attribute.setValue(value);
        contentService.saveAttribute(attribute);
        return "";
    }

    //ajax - update page attribute
    @RequestMapping()
    @ResponseBody
    public String removeAttribute(@RequestParam int id) {
        //TODO: check
        contentService.removeAttribute(id);
        return "";
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //extract the path from requested url
    private String extractEditPath(HttpServletRequest request) {
        String path = "";
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath)) path = requestUri.substring(contextPath.length());
        else path = requestUri;
        //remove the trailing /edit
        path = path.substring(0, path.lastIndexOf("/edit"));
        return path;
    }

    //calculate missing page and template attributes
    private void calculateMissingAttributes(Page page, List missingPageAttributes, List missingTemplateAttributes) {
        if (page.getTemplate() == null) return;
        List<AttributeEnum> missingAttributes = contentService.getTemplate(page.getTemplate().getId()).getAttributeEnumerations(); //attr enums is not included in the page
        List<TemplateAttribute> templateAttributes = page.getTemplate().getTemplateAttributes();
        List<PageAttribute> pageAttributes = page.getPageAttributes();

        //remove already existing template attributes
        for (TemplateAttribute ta : templateAttributes) {
            AttributeEnum attr = new AttributeEnum();
            attr.setAttributeName(ta.getAttribute().getAttribute());
            attr.setAttributeType(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE);
            missingAttributes.remove(attr);
        }

        //remove already existing page attributes
        for (PageAttribute pa : pageAttributes) {
            AttributeEnum attr = new AttributeEnum();
            attr.setAttributeName(pa.getAttribute().getAttribute());
            attr.setAttributeType(AttributeEnum.ATTRIBUTE_TYPE_PAGE);
            missingAttributes.remove(attr);
        }

        //seperate according to type
        for (AttributeEnum ae : missingAttributes) {
            if (ae.getAttributeType().equals(AttributeEnum.ATTRIBUTE_TYPE_PAGE)) missingPageAttributes.add(ae);
            if (ae.getAttributeType().equals(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE)) missingTemplateAttributes.add(ae);
        }
    }

    //calculate missing template attributes
    private void calculateMissingAttributes(Template template, List missingTemplateAttributes) {
        List<TemplateAttribute> templateAttributes = template.getTemplateAttributes();
        List<AttributeEnum> missingAttributes = template.getAttributeEnumerations();

        //remove already existing template attributes
        for (TemplateAttribute ta : templateAttributes) {
            AttributeEnum attr = new AttributeEnum();
            attr.setAttributeName(ta.getAttribute().getAttribute());
            attr.setAttributeType(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE);
            missingAttributes.remove(attr);
        }

        //seperate according to type
        for (AttributeEnum ae : missingAttributes) {
            if (ae.getAttributeType().equals(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE)) missingTemplateAttributes.add(ae);
        }
    }

    //SETTERS
    //--------------------------------------------------------------------------
    @Autowired
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Autowired
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
