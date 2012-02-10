/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.*;
import com.fmguler.cms.service.template.TemplateService;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
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

    //login the user
    @RequestMapping
    public String login(Model model, HttpServletRequest request) {
        String user = (String)request.getSession().getAttribute("user");
        if (user != null) return "redirect:home.htm";

        //check user
        if (request.getMethod().equals("POST")) {
            String username = ServletRequestUtils.getStringParameter(request, "username", "");
            String password = ServletRequestUtils.getStringParameter(request, "password", "");
            if (!password.equals("demo")) {
                //return error message
                String errrorMessage = "Kullanıcı adı ve şifre hatalı.";
                model.addAttribute("errorMessage", errrorMessage);
                return null;
            }

            //TODO: check authentication
            request.getSession().setAttribute("user", "admin");
            String returnUrl = (String)request.getSession().getAttribute("returnUrl");
            if (returnUrl == null) return "redirect:home.htm";
            return "redirect:" + returnUrl;
        }

        return null;
    }

    @RequestMapping
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/admin/login";
    }

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
    public String editPageRedirect(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        String path = "";
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(contextPath)) path = requestUri.substring(contextPath.length());
        else path = requestUri;
        //remove the trailing /edit
        path = path.substring(0, path.lastIndexOf("/edit"));
        if (path.equals("")) return "redirect:/index.html/edit";
        return "redirect:/admin/editPage?path=" + path;
    }

    //edit a page
    @RequestMapping()
    public String editPage(@RequestParam String path, Model model) throws IOException {
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
        pageAttribute.setAttribute(attributeName);
        pageAttribute.setValue("");
        contentService.savePageAttribute(pageAttribute);
        return "";
    }

    //ajax - add template attribute
    @RequestMapping()
    @ResponseBody
    public String addAllPageAttributes(@RequestParam String path) {
        Page page = contentService.getPage(path);
        if (page == null) return "";

        //calculate missing page and template attributes
        List<AttributeEnum> missingPageAttributes = new LinkedList();
        List missingTemplateAttributes = new LinkedList();
        calculateMissingAttributes(page, missingPageAttributes, missingTemplateAttributes);

        for (AttributeEnum attrEnum : missingPageAttributes) {
            addPageAttribute(attrEnum.getAttributeName(), page.getId());
        }

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
        templateAttribute.setAttribute(attributeName);
        templateAttribute.setValue("");
        contentService.saveTemplateAttribute(templateAttribute);
        return "";
    }

    //ajax - save page attribute
    @RequestMapping()
    @ResponseBody
    public String savePageAttribute(@RequestParam String value, @RequestParam int id) {
        //get the attribute
        PageAttribute attribute = contentService.getPageAttribute(id);
        if (attribute == null) return ""; //TODO: return error status
        
        //do not add a new version if the content is exactly same
        if (value.equals(attribute.getValue())) return ""; //TODO: return status

        //add a new version of the attribute
        PageAttribute newAttribute = new PageAttribute();
        newAttribute.setPage(attribute.getPage());
        newAttribute.setAttribute(attribute.getAttribute());
        newAttribute.setValue(value);
        newAttribute.setAuthor("admin");
        newAttribute.setDate(new Date());
        
        newAttribute.setComment("added new version");
        newAttribute.setVersion(attribute.getVersion() + 1);
        contentService.savePageAttribute(newAttribute);
        
        return "";
    }

    //ajax - remove page attribute
    @RequestMapping()
    @ResponseBody
    public String removePageAttribute(@RequestParam int id) {
        PageAttribute attribute = contentService.getPageAttribute(id);
        if (attribute == null) return null; //TODO: return error status
        if (attribute.getVersion() == 0) return null; //cannot delete zeroth attribute (or template will give error)
        
        contentService.removePageAttribute(id);
        return "";
    }

    /**
     * Inject editor code to page html
     * @param pageHtml the page html
     * @return injected page html
     */
    public static String injectEditor(String pageHtml) {
        String disclaimer = "<!-- Edited by fmgCMS -->";
        String editorScript = ""
            + "<script type=\"text/javascript\" src=\"admin/js/jquery-1.7.1.min.js\"></script>\n"
            + "<script type=\"text/javascript\" src=\"admin/js/aloha/lib/aloha.js\" data-aloha-plugins=\"common/format,\n"
            + "common/table,\n"
            + "common/list,\n"
            + "common/link,\n"
            + "common/highlighteditables,\n"
            + "common/block,\n"
            + "common/undo,\n"
            //+ "common/contenthandler,\n"
            //+ "common/paste,\n"
            //+ "common/commands,\n"
            + "common/abbr\">\n"
            + "</script>\n"
            + "<script type=\"text/javascript\" src=\"admin/js/make-editable.js\"></script>\n"
            + "<link href=\"admin/js/aloha/css/aloha.css\" type=\"text/css\" rel=\"stylesheet\" />\n";

        //inject editor code
        StringBuffer pageBuffer = new StringBuffer(pageHtml);
        pageBuffer.insert(0, disclaimer);
        pageBuffer.insert(pageBuffer.indexOf("</head>"), editorScript);

        //replace placeholders with editable
        Pattern pattern = Pattern.compile("\\$\\{(.+)\\}");
        Matcher matcher = pattern.matcher(pageBuffer);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<div id=\"attribute-editable-$1\" onclick=\"window.parent.onAlohaClick('$1')\" class=\"editable\">\\${$1}</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //calculate missing page and template attributes
    private void calculateMissingAttributes(Page page, List missingPageAttributes, List missingTemplateAttributes) {
        if (page.getTemplate() == null) return;
        List<AttributeEnum> missingAttributes = contentService.getTemplate(page.getTemplate().getId()).getAttributeEnumerations(); //attr enums is not included in the page
        List<TemplateAttribute> templateAttributes = page.getTemplate().getTemplateAttributes();
        List<PageAttribute> pageAttributes = page.getPageAttributes();

        //remove already existing template attributes
        for (TemplateAttribute ta : templateAttributes) {
            AttributeEnum attr = new AttributeEnum();
            attr.setAttributeName(ta.getAttribute());
            attr.setAttributeType(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE);
            missingAttributes.remove(attr);
        }

        //remove already existing page attributes
        for (PageAttribute pa : pageAttributes) {
            AttributeEnum attr = new AttributeEnum();
            attr.setAttributeName(pa.getAttribute());
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
            attr.setAttributeName(ta.getAttribute());
            attr.setAttributeType(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE);
            missingAttributes.remove(attr);
        }

        //seperate according to type
        for (AttributeEnum ae : missingAttributes) {
            if (ae.getAttributeType().equals(AttributeEnum.ATTRIBUTE_TYPE_TEMPLATE)) missingTemplateAttributes.add(ae);
        }
    }

    //get all the page and template attributes and return them as map
    private Map getPageAttributesMap(Page page) {
        Map result = new HashMap();

        Iterator it = page.getTemplate().getTemplateAttributes().iterator();
        while (it.hasNext()) {
            TemplateAttribute attribute = (TemplateAttribute)it.next();
            result.put(attribute.getAttribute(), attribute.getValue());
        }

        it = page.getPageAttributes().iterator();
        while (it.hasNext()) {
            PageAttribute attribute = (PageAttribute)it.next();
            result.put(attribute.getAttribute(), attribute.getValue());
        }

        return result;
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
