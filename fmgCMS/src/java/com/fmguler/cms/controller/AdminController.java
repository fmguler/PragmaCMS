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

    //list pages
    @RequestMapping()
    public String pages(Model model) {
        List pages = contentService.getPages();
        model.addAttribute("pages", pages);
        return "admin/pages";
    }

    //list templates
    @RequestMapping()
    public String templates(Model model) {
        List templates = contentService.getTemplates();
        model.addAttribute("templates", templates);
        return "admin/templates";
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

        //if this page is renamed edit the renamed version
        if (page.getNewPath() != null) {
            return "redirect:/admin/editPage?path=" + page.getNewPath();
        }

        //scan and auto add attributes
        scanPageAttributes(page);

        model.addAttribute("page", page);
        model.addAttribute("path", path);
        model.addAttribute("templates", contentService.getTemplates());
        model.addAttribute("pageAttachments", page.getId() == null ? new LinkedList() : contentService.getPageAttachments(page.getId()));
        return "admin/editPage";
    }

    @RequestMapping
    @ResponseBody
    public String savePage(Page page, @RequestParam(required = false) String redirect) {
        if (redirect != null) {
            Page originalPage = contentService.getPage(page.getId());
            if (originalPage == null) return ""; //TODO: error - cannot redirect a new page
            if (originalPage.getPath().equals(page.getPath())) return ""; //trying to redirect to the same path, would cause redirect loop

            //add a new redirect page from old path to the new path
            Page redirectPage = new Page();
            redirectPage.setLastModified(new Date());
            redirectPage.setPath(originalPage.getPath()); //old path
            redirectPage.setNewPath(page.getPath()); //new path
            redirectPage.setTemplate(page.getTemplate());
            contentService.savePage(redirectPage);

            //also update redirects pointing to the old path to the new path (it would still work but we reduce it to single redirect)
            contentService.updatePageRedirects(originalPage.getPath(), page.getPath());
        }

        page.setLastModified(new Date());
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

        model.addAttribute("template", template);
        return "admin/editTemplate";
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

        //update last modified
        Page page = attribute.getPage();
        page.setLastModified(new Date());
        contentService.savePage(page);

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

        //update last modified
        Page page = attribute.getPage();
        page.setLastModified(new Date());
        contentService.savePage(page);

        return "";
    }

    //uploads new item
    @RequestMapping()
    public String uploadPageAttachment(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartReq = (MultipartHttpServletRequest)request;

            //which page this attachment belongs to
            Page page = new Page();
            page.setId(Integer.valueOf(request.getParameter("page.id")));

            Iterator it = multipartReq.getFileNames();
            while (it.hasNext()) {
                String fileName = (String)it.next();
                MultipartFile multipartFile = multipartReq.getFile(fileName);
                String originalName = multipartFile.getOriginalFilename();

                //Create new workspace item
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
                    continue; //TODO: inform the user
                } catch (IOException ex) {
                    Logger.getLogger(ContentController.class.getName()).log(Level.SEVERE, "Cannot read from multipart request input stream", ex);
                    continue; //TODO: inform the user
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

        return "redirect:/admin/editPage?path=" + request.getParameter("page.path");
    }

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
                + "<script type=\"text/javascript\" src=\"admin/js/jquery-1.7.1.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"admin/js/jquery-ui-1.8.16.custom.min.js\"></script>\n"
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
                + "<link href=\"admin/js/jquery-ui-base/jquery.ui.all.css\" type=\"text/css\" rel=\"stylesheet\" />\n"
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

        //new page, template not selected yet
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
