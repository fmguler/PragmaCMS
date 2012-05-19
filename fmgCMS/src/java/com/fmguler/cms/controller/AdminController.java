/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.helper.CommonController;
import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.*;
import com.fmguler.cms.service.template.TemplateService;
import com.fmguler.common.service.storage.StorageException;
import com.fmguler.common.service.storage.StorageService;
import com.fmguler.common.service.storage.domain.StorageObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Privileged operations for admins.
 * @author Fatih Mehmet Güler
 */
@Controller
public class AdminController {
    private ContentService contentService;
    private TemplateService templateService;
    private StorageService storageService;

    //login the user
    @RequestMapping
    public String login(Model model, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        if (user != null) return "redirect:home.htm";

        //on post
        if (request.getMethod().equals("POST")) {
            String username = ServletRequestUtils.getStringParameter(request, "username", "");
            String password = ServletRequestUtils.getStringParameter(request, "password", "");

            //check user existence & authentication
            user = contentService.getAuthor(username);
            if (user == null || !user.checkPassword(password)) {
                //return error message
                String errrorMessage = "Username/password incorrect!";
                model.addAttribute("errorMessage", errrorMessage);
                return null;
            }

            //attach user to session
            request.getSession().setAttribute("user", user);

            //return back to the desired url
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
        return "redirect:/admin/pages";
    }

    //without any method - direct entrance like /admin
    @RequestMapping()
    public String admin(Model model) {
        System.out.println("direct entrance");
        return "redirect:/admin/pages";
    }

    //--------------------------------------------------------------------------
    //PAGES
    //--------------------------------------------------------------------------
    //list pages
    @RequestMapping()
    public String pages(Model model) {
        List pages = contentService.getPages();
        List templates = contentService.getTemplates();
        model.addAttribute("pages", pages);
        model.addAttribute("templates", templates);
        return "admin/pages";
    }

    @RequestMapping
    @ResponseBody
    public String addPage(Page page) {
        if (page.getId() != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page already exists", null);
        if (!page.getPath().startsWith("/")) page.setPath("/" + page.getPath());
        if (!page.getPath().substring(1).matches("[A-Za-z0-9\\-]{0,61}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page path should not include any special character. Valid characters are; <br/><li>Letters (a-z/A-Z)<br/><li>Numbers (0-9)<br/><li>Dash (-)", null);
        if (contentService.getPage(page.getPath()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A page with this path already exists", null);

        //save the page
        page.setLastModified(new Date());
        contentService.savePage(page);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", page);
    }

    //remove page
    @RequestMapping
    @ResponseBody
    public String removePage(@RequestParam int pageId, Model model) {
        try {
            contentService.removePage(pageId);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //TEMPLATES
    //--------------------------------------------------------------------------
    //list templates
    @RequestMapping()
    public String templates(Model model) {
        List templates = contentService.getTemplates();
        model.addAttribute("templates", templates);
        return "admin/templates";
    }

    //--------------------------------------------------------------------------
    //EDIT PAGE
    //--------------------------------------------------------------------------
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
        if (page == null) {
            model.addAttribute("errorMessage", "This page does not exist. You should add the page first.");
            model.addAttribute("errorAction", "Add Page");
            model.addAttribute("errorActionUrl", "pages?addPage=" + path);
            return "admin/error";
        }

        //if this page is renamed edit the renamed version
        if (page.getNewPath() != null) {
            return "redirect:/admin/editPage?path=" + page.getNewPath();
        }

        //scan and auto add attributes
        scanPageAttributes(page);

        model.addAttribute("page", page);
        return "admin/editPage";
    }

    //ajax - get page
    @RequestMapping()
    @ResponseBody
    public String getPage(@RequestParam Integer pageId) {
        Page page = contentService.getPage(pageId);
        if (page == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", page);
    }

    //ajax - rename page
    @RequestMapping
    @ResponseBody
    public String renamePage(@RequestParam Integer pageId, @RequestParam String newPath, @RequestParam(required = false) String redirect) {
        Page originalPage = contentService.getPage(pageId);
        if (originalPage == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null); //error - cannot redirect a new page
        if (contentService.getPage(newPath) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A page with this address already exists!", null);
        if (originalPage.getPath().equals(newPath)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "You did not change the address...", null); //also this could cause redirect loop

        //path validity checks (same as addPage)
        if (!newPath.startsWith("/")) newPath = "/" + newPath;
        if (!newPath.substring(1).matches("[A-Za-z0-9\\-]{0,61}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page path should not include any special character. Valid characters are; <br/><li>Letters (a-z/A-Z)<br/><li>Numbers (0-9)<br/><li>Dash (-)", null);

        //add a redirect page with old path pointing to renamed page
        if (redirect != null) {
            //add a new redirect page from old path to the new path
            Page redirectPage = new Page();
            redirectPage.setLastModified(new Date());
            redirectPage.setPath(originalPage.getPath()); //old path
            redirectPage.setNewPath(newPath); //new path
            redirectPage.setTemplate(originalPage.getTemplate());
            contentService.savePage(redirectPage);
        }

        //also update redirects pointing to the old path to the new path (it would still work but we reduce it to single redirect)
        contentService.updatePageRedirects(originalPage.getPath(), newPath);

        //save the page with new path
        originalPage.setLastModified(new Date());
        originalPage.setPath(newPath);
        contentService.savePage(originalPage);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Page is renamed successfully", null);
    }

    //ajax - save all page attributes
    @RequestMapping()
    @ResponseBody
    public String savePageAttributes(@RequestParam Integer pageId, @RequestParam String comment, @RequestParam Boolean publish, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        Page page = contentService.getPage(pageId);
        if (page == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);

        //save updated page attributes
        List savedAttributes = new LinkedList();
        Date now = new Date(); //all changes have same date (to view by date in history)
        List<PageAttribute> pageAttributes = page.getPageAttributes();
        for (PageAttribute attribute : pageAttributes) {
            String attributeValue = request.getParameter("attribute-" + attribute.getId());
            if (attributeValue == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "No value specified for: " + attribute.getAttribute(), null);

            //do not add a new version if the content is exactly same
            if (attributeValue.equals(attribute.getValue())) continue;

            //update the value and add history attribute (revision)
            PageAttributeHistory attributeHistory = new PageAttributeHistory();
            attributeHistory.setPage(page);
            attributeHistory.setAttribute(attribute.getAttribute());
            attributeHistory.setValue(attributeValue);
            attributeHistory.setAuthor(user.getUsername());
            attributeHistory.setDate(now);
            attributeHistory.setComment(comment);
            attributeHistory.setVersion(null); //do not try to select max(version), instead we just sort by date and number from 1 to n
            contentService.savePageAttributeHistory(attributeHistory);

            //update attribute value and version if this is not a draft
            if (publish) {
                attribute.setPage(page); //does not include page because it's got from page
                attribute.setValue(attributeHistory.getValue());
                attribute.setAuthor(attributeHistory.getAuthor());
                attribute.setDate(attributeHistory.getDate());
                attribute.setComment(attributeHistory.getComment());
                attribute.setVersion(attributeHistory.getId()); //to detect which history attribute this current attribute corresponds to
                contentService.savePageAttribute(attribute);
                attribute.setPage(null); //to prevent loop for gson
            }

            //to notify user which atts are saved
            savedAttributes.add(attribute.getAttribute());
        }

        //nothing to save
        if (savedAttributes.isEmpty()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "No attribute is changed, nothing to save", null);

        //update page last modified if this is not a draft
        if (publish) {
            page.setLastModified(new Date());
            contentService.savePage(page);
        }

        //return success
        Map result = new HashMap();
        result.put("savedAttributes", savedAttributes);
        result.put("page", page);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Saved following attributes successfully;", result);
    }

    //ajax - page attribute histories
    @RequestMapping()
    @ResponseBody
    public String getPageAttributeHistories(@RequestParam Integer pageId, @RequestParam String attribute) {
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getPageAttributeHistories(pageId, attribute));
    }

    //ajax - revert page attribute to history version
    @RequestMapping()
    @ResponseBody
    public String revertPageAttribute(@RequestParam Integer attributeId, @RequestParam Integer attributeHistoryId) {
        PageAttribute attribute = contentService.getPageAttribute(attributeId);
        PageAttributeHistory attributeHistory = contentService.getPageAttributeHistory(attributeHistoryId);
        if (attribute == null || attributeHistory == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Attribute not found", null);

        //revert to this history version
        attribute.setValue(attributeHistory.getValue());
        attribute.setAuthor(attributeHistory.getAuthor());
        attribute.setDate(attributeHistory.getDate());
        attribute.setComment(attributeHistory.getComment());
        attribute.setVersion(attributeHistory.getId()); //to detect which history attribute this current attribute corresponds to
        contentService.savePageAttribute(attribute);

        //update last modified of the page
        Page page = attribute.getPage();
        page.setLastModified(new Date());
        contentService.savePage(page);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Attribute is reverted successfully", null);
    }

    //ajax - get page attachments
    @RequestMapping()
    @ResponseBody
    public String getPageAttachments(@RequestParam Integer pageId) {
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getPageAttachments(pageId));
    }

    //ajax - uploads page attachment
    @RequestMapping()
    @ResponseBody
    public String uploadPageAttachment(@RequestParam Integer pageId, HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartReq = (MultipartHttpServletRequest)request;

            //which page this attachment belongs to
            Page page = new Page();
            page.setId(pageId);

            Iterator it = multipartReq.getFileNames();
            while (it.hasNext()) {
                String fileName = (String)it.next();
                MultipartFile multipartFile = multipartReq.getFile(fileName);
                String originalName = multipartFile.getOriginalFilename();

                //Create new page attachment
                PageAttachment pageAttachment = new PageAttachment();
                pageAttachment.setPage(page);
                pageAttachment.setContentKey(storageService.generateKey());
                pageAttachment.setName(originalName);
                String guessedContentType = URLConnection.guessContentTypeFromName(originalName);
                pageAttachment.setContentType(guessedContentType == null ? multipartFile.getContentType() : guessedContentType);
                pageAttachment.setLastModified(new Date());

                //pipe uploaded data to attachment output stream (original upload)
                InputStream is = null;
                OutputStream os = null;
                try {
                    os = storageService.getOutputStream(pageAttachment.getContentKey());
                    is = multipartFile.getInputStream();
                    IOUtils.copyLarge(is, os);
                } catch (StorageException ex) {
                    Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Storage service error at uploadPageAttachment", ex);
                    return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Error uploading attachment: " + ex.getMessage(), null);
                } catch (IOException ex) {
                    Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Cannot read from multipart request input stream", ex);
                    return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Error uploading attachment: " + ex.getMessage(), null);
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(os);
                }

                //set the content length
                StorageObject storageObject = storageService.getStorageObject(pageAttachment.getContentKey());
                pageAttachment.setContentLength(storageObject.getSize());

                //save the workspace item
                contentService.savePageAttachment(pageAttachment);
            }
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Attachment is uploaded successfully", contentService.getPageAttachments(pageId));
    }

    //remove page attachment
    @RequestMapping
    @ResponseBody
    public String removePageAttachment(@RequestParam int attachmentId) {
        try {
            contentService.removePageAttachment(attachmentId);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page attachment could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //EDIT TEMPLATE
    //--------------------------------------------------------------------------
    //edit a template
    @RequestMapping()
    public String editTemplate(@RequestParam int id, Model model) {
        Template template = contentService.getTemplate(id);

        //return 404
        if (template == null) {
            return null;
        }

        model.addAttribute("template", template);
        return "admin/editTemplate";
    }

    //--------------------------------------------------------------------------
    //ADVANCED
    //--------------------------------------------------------------------------
    //generate sitemap xml
    @RequestMapping()
    @ResponseBody()
    public String generateSitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //get the http://something part
        String urlPrefix = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length());
        response.setContentType("text/xml");

        StringBuffer sitemapBuffer = new StringBuffer();
        sitemapBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        //add all pages with last modified date
        List<Page> pages = contentService.getPages();
        for (Page page : pages) {
            sitemapBuffer.append("<url>");
            sitemapBuffer.append("<loc>").append(urlPrefix).append(page.getPath()).append("</loc>");
            sitemapBuffer.append("<lastmod>").append(new SimpleDateFormat("yyyy-MM-dd").format(page.getLastModified())).append("</lastmod>");
            sitemapBuffer.append("<changefreq>daily</changefreq>");
            sitemapBuffer.append("<priority>0.8</priority>");
            sitemapBuffer.append("</url>");
        }

        sitemapBuffer.append("</urlset>");
        return sitemapBuffer.toString();
    }

    /**
     * Inject editor code to page html
     * @param pageHtml the page html
     * @return injected page html
     */
    public static String injectEditor(String pageHtml) {
        String disclaimer = "<!-- Edited by fmgCMS -->";
        String editorScript = ""
                + "<script type=\"text/javascript\" src=\"admin/js/jquery-1.7.2.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"admin/js/jquery-ui-1.8.20.custom.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"admin/js/aloha/lib/aloha.js\" data-aloha-plugins=\"common/format,\n"
                + "common/table,\n"
                + "common/list,\n"
                + "common/link,\n"
                + "common/highlighteditables,\n"
                + "common/block,\n"
                + "common/undo,\n"
                + "common/fmg\n"
                //+ "common/image\n"
                //+ "common/contenthandler,\n"
                //+ "common/paste,\n"
                //+ "common/commands,\n"
                //+ "common/abbr\">\n"
                + "\">\n"
                + "</script>\n"
                + "<script type=\"text/javascript\" src=\"admin/js/make-editable.js\"></script>\n"
                + "<link href=\"admin/js/jquery-ui-base/jquery-ui-1.8.20.custom.css\" type=\"text/css\" rel=\"stylesheet\" />\n"
                + "<link href=\"admin/js/aloha/css/aloha.css\" type=\"text/css\" rel=\"stylesheet\" />\n";

        //inject editor code
        StringBuffer pageBuffer = new StringBuffer(pageHtml);
        pageBuffer.insert(0, disclaimer);
        pageBuffer.insert(pageBuffer.indexOf("</head>"), editorScript);

        //replace placeholders with editable
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}"); //lazy match, to match multiple attrs in one line
        Matcher matcher = pattern.matcher(pageBuffer);
        matcher.region(pageBuffer.indexOf("<body>"), pageBuffer.length()); //do not inject to invisible region (head)
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            if (attribute.startsWith("i_")) continue; //do not edit invisible attributes
            if (attribute.startsWith("t_")) continue; //do not edit template attributes
            matcher.appendReplacement(sb, "<div id=\"attribute-editable-$1\" onclick=\"window.parent.onAlohaClick('$1')\" class=\"editable\">\\${$1}</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //scan page attributes
    private void scanPageAttributes(Page page) {
        //scan the page for ${} placeholders
        //detect the missing page attributes, and add them to the page
        //detect unused attributes and mark them

        //new page, template not selected yet (this shouldn't happen anymore, but be safe)
        if (page.getTemplate() == null) return;

        String templatePath = page.getTemplate().getName();
        String templateSource = templateService.getTemplateSource(templatePath);

        //scan template source for attributes
        Set<String> pageAttributes = new HashSet();
        Set<String> templateAttributes = new HashSet();
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(templateSource);
        while (matcher.find()) {
            String attribute = matcher.group(1);
            if (attribute.startsWith("t_")) templateAttributes.add(attribute);
            else pageAttributes.add(attribute);
        }

        //detect missing attributes
        List<PageAttribute> existingPageAttributes = page.getPageAttributes();
        for (PageAttribute attr : existingPageAttributes) {
            if (pageAttributes.contains(attr.getAttribute())) pageAttributes.remove(attr.getAttribute());
        }

        //detect missing template attributes
        List<TemplateAttribute> existingTemplateAttributes = page.getTemplate().getTemplateAttributes();
        for (TemplateAttribute attr : existingTemplateAttributes) {
            if (templateAttributes.contains(attr.getAttribute())) templateAttributes.remove(attr.getAttribute());
        }

        //add missing attributes to page
        for (String attribute : pageAttributes) {
            PageAttribute pageAttribute = new PageAttribute();
            pageAttribute.setAttribute(attribute);
            pageAttribute.setAuthor("admin");
            pageAttribute.setComment("attribute is scanned");
            pageAttribute.setDate(new Date());
            pageAttribute.setPage(page);
            pageAttribute.setValue("");
            pageAttribute.setVersion(0);
            contentService.savePageAttribute(pageAttribute);
            page.getPageAttributes().add(pageAttribute);
        }

        //add missing attributes to template
        for (String attribute : templateAttributes) {
            TemplateAttribute templateAttribute = new TemplateAttribute();
            templateAttribute.setAttribute(attribute);
            templateAttribute.setAuthor("admin");
            templateAttribute.setComment("attribute is scanned");
            templateAttribute.setDate(new Date());
            templateAttribute.setTemplate(page.getTemplate());
            templateAttribute.setValue("");
            templateAttribute.setVersion(0);
            contentService.saveTemplateAttribute(templateAttribute);
            page.getTemplate().getTemplateAttributes().add(templateAttribute);
        }
    }

    //scan template attributes
    private void scanTemplateAttributes(Template template) {
        //scan the template for ${} placeholders, use some convention for template and global attrs
        //detect the missing template attributes, and add them to the template
        //detect unused attributes and mark them
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

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }
}
