/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.controller;

import com.fmguler.cms.helper.CommonController;
import com.fmguler.cms.service.account.AccountService;
import com.fmguler.cms.service.account.domain.Account;
import com.fmguler.cms.service.account.domain.Author;
import com.fmguler.cms.service.content.ContentService;
import com.fmguler.cms.service.content.domain.*;
import com.fmguler.cms.service.resource.ResourceException;
import com.fmguler.cms.service.resource.ResourceService;
import com.fmguler.cms.service.resource.domain.Resource;
import com.fmguler.cms.service.template.TemplateException;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    private ResourceService resourceService;
    private AccountService accountService;
    private StorageService storageService;

    //login the user
    @RequestMapping
    public String login(Model model, HttpServletRequest request, HttpServletResponse response, Site site) {
        Author user = (Author)request.getSession().getAttribute("user");
        if (user != null) return "redirect:home.htm";

        //on post
        if (request.getMethod().equals("POST")) {
            String username = ServletRequestUtils.getStringParameter(request, "username", "");
            String password = ServletRequestUtils.getStringParameter(request, "password", "");

            //check user existence & authentication
            user = accountService.getAuthor(username);
            if (user == null || !user.checkPassword(password)) {
                //return error message
                String errrorMessage = "Username/password incorrect!";
                model.addAttribute("errorMessage", errrorMessage);
                return "admin/login";
            }

            //check if correct site
            if (!user.getAccount().checkSite(site.getId())) {
                //return error message
                String errorMessage = "This web site does not belong to your account, you can login to one of these domains; ";
                for (Object o : user.getAccount().getSites()) {
                    Site s = (Site)o;
                    errorMessage += s.getDomains() + " ";
                }
                model.addAttribute("errorMessage", errorMessage);
                return "admin/login";
            }

            //attach user to session
            request.getSession().setAttribute("user", user);

            //log as info
            Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Site: {0} ({1}) User logged in: {2}", new Object[]{request.getServerName(), site.getId(), user.getUsername()});

            //return back to the desired url
            String returnUrl = (String)request.getSession().getAttribute("returnUrl");
            if (returnUrl == null) return "redirect:home.htm";
            return "redirect:" + returnUrl;
        }

        return "admin/login";
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
    public String pages(Model model, Site site) {
        List pages = contentService.getPages(site.getId());
        List templates = contentService.getTemplates(site.getId());
        model.addAttribute("pages", pages);
        model.addAttribute("templates", templates);
        return "admin/pages";
    }

    @RequestMapping
    @ResponseBody
    public String addPage(Page page, Site site) {
        if (page.getId() != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page already exists", null);
        if (!page.getPath().startsWith("/")) page.setPath("/" + page.getPath());
        if (!page.getPath().substring(1).matches("[A-Za-z0-9\\-./]{0,255}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page path should not include any special character. Valid characters are; <br/><li>Letters (a-z/A-Z)<br/><li>Numbers (0-9)<li>Dash (-)<li>Dot (.)", null);
        if (contentService.getPage(page.getPath(), site.getId()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A page with this path already exists", null);

        //save the page
        page.setLastModified(new Date());
        page.setSite(site);
        contentService.savePage(page);

        //to get page attributes and template
        page = contentService.getPage(page.getId());

        //scan and auto add attributes
        scanAttributes(page, site);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", page);
    }

    //remove page
    @RequestMapping
    @ResponseBody
    public String removePage(@RequestParam int pageId, Model model, Site site) {
        try {
            Page page = contentService.getPage(pageId);
            if (page == null || !page.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);

            contentService.removePage(pageId);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //EDIT PAGE
    //--------------------------------------------------------------------------
    //edit a page
    @RequestMapping("/**/edit")
    public String editPageRedirect(HttpServletRequest request, HttpServletResponse response, Model model) {
        String path;
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
    public String editPage(@RequestParam String path, Model model, Site site) {
        Page page = contentService.getPage(path, site.getId());

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

        //log as info
        Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Site: {0} Editing Page: {1}", new Object[]{site.getId(), path});

        model.addAttribute("page", page);
        return "admin/editPage";
    }

    //ajax - get page
    @RequestMapping()
    @ResponseBody
    public String getPage(@RequestParam Integer pageId, Site site) {
        Page page = contentService.getPage(pageId);
        if (page == null || !page.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", page);
    }

    //ajax - rename page
    @RequestMapping
    @ResponseBody
    public String renamePage(@RequestParam Integer pageId, @RequestParam String newPath, @RequestParam(required = false) String redirect, Site site) {
        Page originalPage = contentService.getPage(pageId);
        if (originalPage == null || !originalPage.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null); //error - cannot redirect a new page
        if (contentService.getPage(newPath, site.getId()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A page with this address already exists!", null);
        if (originalPage.getPath().equals(newPath)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "You did not change the address...", null); //also this could cause redirect loop

        //path validity checks (same as addPage)
        if (!newPath.startsWith("/")) newPath = "/" + newPath;
        if (!newPath.substring(1).matches("[A-Za-z0-9\\-./]{0,255}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page path should not include any special character. Valid characters are; <br/><li>Letters (a-z/A-Z)<br/><li>Numbers (0-9)<br/><li>Dash (-)<li>Dot (.)", null);

        //add a redirect page with old path pointing to renamed page
        if (redirect != null) {
            //add a new redirect page from old path to the new path
            Page redirectPage = new Page();
            redirectPage.setLastModified(new Date());
            redirectPage.setPath(originalPage.getPath()); //old path
            redirectPage.setNewPath(newPath); //new path
            redirectPage.setTemplate(originalPage.getTemplate());
            redirectPage.setSite(site);
            contentService.savePage(redirectPage);
        }

        //also update redirects pointing to the old path to the new path (it would still work but we reduce it to single redirect)
        contentService.updatePageRedirects(originalPage.getPath(), newPath, site.getId());

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
    public String savePageAttributes(@RequestParam Integer pageId, @RequestParam String comment, @RequestParam Boolean publish, HttpServletRequest request, Site site) {
        Author user = (Author)request.getSession().getAttribute("user");
        Page page = contentService.getPage(pageId);
        if (page == null || !page.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);

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
    public String getPageAttributeHistories(@RequestParam Integer pageId, @RequestParam String attribute, Site site) {
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getPageAttributeHistories(pageId, attribute, site.getId()));
    }

    //ajax - revert page attribute to history version
    @RequestMapping()
    @ResponseBody
    public String revertPageAttribute(@RequestParam Integer attributeId, @RequestParam Integer attributeHistoryId, Site site) {
        PageAttribute attribute = contentService.getPageAttribute(attributeId);
        PageAttributeHistory attributeHistory = contentService.getPageAttributeHistory(attributeHistoryId);
        if (attribute == null || attributeHistory == null || !attribute.getPage().checkSite(site) || !attributeHistory.getPage().checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Attribute not found", null);

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

    //ajax - remove page attribute history
    @RequestMapping()
    @ResponseBody
    public String removePageAttributeHistory(@RequestParam Integer attributeHistoryId, Site site) {
        PageAttributeHistory attributeHistory = contentService.getPageAttributeHistory(attributeHistoryId);
        if (attributeHistory == null || !attributeHistory.getPage().checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Attribute History not found", null);
        contentService.removePageAttributeHistory(attributeHistoryId);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Attribute History is removed successfully", null);
    }

    //ajax - get page attachments
    @RequestMapping()
    @ResponseBody
    public String getPageAttachments(@RequestParam Integer pageId, Site site) {
        Page page = contentService.getPage(pageId);
        if (page == null || !page.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);

        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getPageAttachments(pageId));
    }

    //ajax - uploads page attachment
    @RequestMapping()
    @ResponseBody
    public String uploadPageAttachment(@RequestParam Integer pageId, HttpServletRequest request, Site site) {
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartReq = (MultipartHttpServletRequest)request;

            //which page this attachment belongs to
            Page page = contentService.getPage(pageId);
            if (page == null || !page.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page not found", null);

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
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Storage service error at uploadPageAttachment", ex);
                    return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Error uploading attachment: " + ex.getMessage(), null);
                } catch (IOException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Cannot read from multipart request input stream", ex);
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
    public String removePageAttachment(@RequestParam int attachmentId, Site site) {
        try {
            PageAttachment pageAttachment = contentService.getPageAttachment(attachmentId);
            if (pageAttachment == null || !pageAttachment.getPage().checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page attachment not found", null);
            contentService.removePageAttachment(attachmentId);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This page attachment could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //STATIC RESOURCES
    //--------------------------------------------------------------------------
    //list resources
    @RequestMapping()
    public String resources(Model model, @RequestParam(required = false) String resourceFolder, @RequestParam(required = false) String addFolder, Site site) {
        try {
            //partition the add folder /a/b/c to /a/b and c
            if (addFolder != null && addFolder.contains("/")) {
                resourceFolder = addFolder.substring(0, addFolder.lastIndexOf("/"));
                addFolder = addFolder.substring(addFolder.lastIndexOf("/") + 1);
            }

            //check null
            if (resourceFolder == null) resourceFolder = "";

            //check not exists
            Resource folder = resourceService.getResource(toRootFolder(site), resourceFolder);
            if (folder == null || !folder.getDirectory()) {
                model.addAttribute("errorMessage", "This folder does not exist. Do you want to create this folder?");
                model.addAttribute("errorAction", "Create Folder");
                model.addAttribute("errorActionUrl", "resources?addFolder=" + resourceFolder);
                return "admin/error";
            }

            //get the resources in this folder, files and folders
            List resources = resourceService.getResources(toRootFolder(site), folder);
            model.addAttribute("resources", resources);
            model.addAttribute("resourceFolder", folder.toResourcePath());
            model.addAttribute("resourceFolderArray", folder.toResourcePath().split("/"));
            model.addAttribute("addFolderParam", addFolder);
            return "admin/resources";
        } catch (ResourceException ex) {
            model.addAttribute("errorMessage", "Unknown Error. Please report this to us: " + ex.getMessage());
            model.addAttribute("errorAction", "Go Back");
            model.addAttribute("errorActionUrl", "resources");
            return "admin/error";
        }
    }

    //download resource
    @RequestMapping()
    public void downloadResource(@RequestParam String resourcePath, HttpServletRequest request, HttpServletResponse response, Site site) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), resourcePath);

            //check not exists
            if (resource == null || resource.getDirectory()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            //pipe the resource as attachment
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Content-Disposition", "attachment; filename=" + resource.getName());
            response.setContentLength(resource.getContentLength());
            inputStream = resourceService.getInputStream(toRootFolder(site), resource);
            outputStream = response.getOutputStream();
            IOUtils.copyLarge(inputStream, outputStream);
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException while downloading resource", ex);
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Cannot write static resource to response output stream", ex);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    //upload resource
    @RequestMapping()
    public String uploadResource(@RequestParam String resourceFolder, HttpServletRequest request, Model model, Site site) {
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multipartReq = (MultipartHttpServletRequest)request;

            //get the folder
            Resource folder = resourceService.getResource(toRootFolder(site), resourceFolder);

            //check folder
            if (folder == null || !folder.getDirectory()) {
                model.addAttribute("errorMessage", "This folder does not exist. Do you want to create this folder?");
                model.addAttribute("errorAction", "Create Folder");
                model.addAttribute("errorActionUrl", "resources?addFolder=" + folder.toResourcePath());
                return "admin/error";
            }

            //handle upload
            Iterator it = multipartReq.getFileNames();
            while (it.hasNext()) {
                String fileName = (String)it.next();
                MultipartFile multipartFile = multipartReq.getFile(fileName);
                String originalName = multipartFile.getOriginalFilename();

                //the uploaded resource NOTE: resource service will overwrite existing resources
                Resource uploadedResource = new Resource();
                uploadedResource.setFolder(folder.toResourcePath());
                uploadedResource.setName(originalName);
                uploadedResource.setDirectory(false);

                //pipe uploaded data to resource output stream (original upload)
                InputStream is = null;
                OutputStream os = null;
                try {
                    os = resourceService.getOutputStream(toRootFolder(site), uploadedResource);
                    is = multipartFile.getInputStream();
                    IOUtils.copyLarge(is, os);
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(os);

                    //if this is a zip file, extract it
                    if (originalName.endsWith(".zip")) {
                        resourceService.extractZip(toRootFolder(site), uploadedResource, false, true);
                    }
                } catch (ResourceException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Resource service error at uploadResource", ex);
                    model.addAttribute("errorMessage", "Unknown Error. Please report this to us: " + ex.getMessage());
                    model.addAttribute("errorAction", "Go Back");
                    model.addAttribute("errorActionUrl", "resources?resourceFolder=" + folder.toResourcePath());
                    return "admin/error";
                } catch (IOException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Cannot read from multipart request input stream", ex);
                    model.addAttribute("errorMessage", "Unknown Error. Please report this to us: " + ex.getMessage());
                    model.addAttribute("errorAction", "Go Back");
                    model.addAttribute("errorActionUrl", "resources?resourceFolder=" + folder.toResourcePath());
                    return "admin/error";
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(os);
                }
            }

            //return success
            return "redirect:resources?resourceFolder=" + folder.toResourcePath();
        }

        //not multipart
        return null;
    }

    //ajax - add folder
    @RequestMapping
    @ResponseBody
    public String addFolder(@RequestParam String baseFolder, @RequestParam String name, Site site) {
        try {
            Resource folder = resourceService.getResource(toRootFolder(site), baseFolder);
            if (folder == null || !folder.getDirectory()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Base folder does not exist", null);

            //check name for special chars
            if (!name.matches("[\\w\\- ]{0,100}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Folder name contains invalid characters", null);

            //add the folder
            resourceService.addFolder(toRootFolder(site), folder, name);

            //return success
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (ResourceException ex) {
            if (ex.getErrorCode().equals(ResourceException.ERROR_FOLDER_ALREADY_EXISTS)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Folder already exists", null);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add folder: " + ex.getMessage(), null);
        }
    }

    //ajax - remove resouce
    @RequestMapping
    @ResponseBody
    public String removeResource(@RequestParam String resourcePath, Site site) {
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), resourcePath);
            if (resource == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Resource does not exist", null);

            //remove the resource (also removes folders)
            resourceService.removeResource(toRootFolder(site), resource);

            //return success
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (ResourceException ex) {
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not remove resource: " + ex.getMessage(), null);
        }
    }

    //ajax - crawl web page
    @RequestMapping
    @ResponseBody
    public String crawlWebPage(@RequestParam String baseFolder, @RequestParam String pageUrl, @RequestParam(required = false) String followLinks, Site site) {
        try {
            Resource folder = resourceService.getResource(toRootFolder(site), baseFolder);
            if (folder == null || !folder.getDirectory()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Base folder does not exist", null);

            //crawl web page, asynchronously
            resourceService.crawlWebPage(toRootFolder(site), folder, pageUrl, followLinks != null);

            //return success
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Started crawling web page. Reload page to see crawled resources.", null);
        } catch (ResourceException ex) {
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not crawl web page, error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //TEMPLATES
    //--------------------------------------------------------------------------
    //list templates
    @RequestMapping()
    public String templates(Model model, Site site) {
        List templates = contentService.getTemplates(site.getId());
        model.addAttribute("templates", templates);
        return "admin/templates";
    }

    @RequestMapping
    @ResponseBody
    public String addTemplate(@RequestParam String name, @RequestParam String path, Site site) {
        Resource resource = resourceService.getResource(toRootFolder(site), path);
        if (resource == null || resource.getDirectory() || !(resource.getName().endsWith(".htm") || resource.getName().endsWith(".html"))) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "No HTML resource exists for given path.", null);
        if (contentService.getTemplate(path, site.getId()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Another template with this resource path already exists. If you want to use the same resource you can duplicate it in the resources menu.", null);

        //save the template
        Template template = new Template();
        template.setName(name);
        template.setPath(resource.toResourcePath());
        template.setSite(site);
        contentService.saveTemplate(template);

        //copy the resource html as template file
        InputStream is = null;
        OutputStream os = null;
        String templateHtml;
        try {
            is = resourceService.getInputStream(toRootFolder(site), resource);
            templateHtml = IOUtils.toString(is, "UTF-8");
            Document templateDocument = Jsoup.parse(templateHtml);
            templateDocument.outputSettings().prettyPrint(false);

            //add initial history record
            TemplateHistory templateHistory = new TemplateHistory();
            templateHistory.setTemplate(template);
            templateHistory.setHtml(templateHtml);
            templateHistory.setAuthor("admin");
            templateHistory.setComment("template is added");
            templateHistory.setDate(new Date());
            contentService.saveTemplateHistory(templateHistory);

            //save processed form as template file
            os = templateService.getSourceOutputStream(template.getId() + ".ftl");
            os.write(processTemplate(templateDocument, resource, true, site).getBytes("UTF-8"));
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at addTemplate cannot read resource", ex);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
        } catch (TemplateException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at addTemplate cannot write to template source os", ex);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at addTemplate cannot write to template source os", ex);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", template);
    }

    //remove template
    @RequestMapping
    @ResponseBody
    public String removeTemplate(@RequestParam int templateId, Model model, Site site) {
        try {
            Template template = contentService.getTemplate(templateId);
            if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);
            contentService.removeTemplate(templateId);
            templateService.removeSource(templateId + ".ftl");
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This template could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //EDIT TEMPLATE
    //--------------------------------------------------------------------------
    //edit a template
    @RequestMapping()
    public String editTemplate(@RequestParam int id, @RequestParam(required = false) String ofPage, Model model, HttpServletRequest request, Site site) {
        Template template;

        //if ofPage parameter is set, edit the template of this page
        if (ofPage != null) {
            Page page = contentService.getPage(ofPage, site.getId());
            template = page == null ? null : page.getTemplate();
        } else template = contentService.getTemplate(id);

        //no such template
        if (template == null || !template.checkSite(site)) {
            model.addAttribute("errorMessage", "This template does not exist. You should add the template first.");
            model.addAttribute("errorAction", "Add Template");
            model.addAttribute("errorActionUrl", "templates");
            return "admin/error";
        }

        //get the original unmodified html (scripts can modify DOM)
        InputStream inputStream = null;
        String templateHtml = null;
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            //TODO: check resource null, the resource this template points to may have been renamed
            inputStream = resourceService.getInputStream(toRootFolder(site), resource);
            templateHtml = IOUtils.toString(inputStream, "UTF-8");
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error getting the template resource for templateHtml", ex);
            model.addAttribute("errorMessage", "Unknown Error. Please report this to us: " + ex.getMessage());
            model.addAttribute("errorAction", "Go Back");
            model.addAttribute("errorActionUrl", "resources");
            return "admin/error";
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error reading template resource for templateHtml", ex);
            model.addAttribute("errorMessage", "Unknown Error. Please report this to us: " + ex.getMessage());
            model.addAttribute("errorAction", "Go Back");
            model.addAttribute("errorActionUrl", "resources");
            return "admin/error";
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        //NOTE: we add custom attributes to all elements to detect where to merge changes
        //BECAUSE: scripts can modify DOM, we cannot know which element corresponds to which element in the original html
        Document templateDocument = Jsoup.parse(templateHtml);
        templateDocument.outputSettings().prettyPrint(false);
        templateDocument.head().append("<script id=\"fmgcms-injected-script\" type=\"text/javascript\" src=\"" + request.getContextPath() + "/admin/js/firebug-lite/build/firebug-lite-debug.js\"></script>");
        int templateElemCounter = 0;
        Elements allElements = templateDocument.getAllElements();
        for (Element elem : allElements) {
            if (elem.tagName().equals("html")) continue; //do not directly edit html tag
            elem.attr("fmgcms-id", "" + templateElemCounter);
            templateElemCounter++;
        }

        //will be loaded by preview iframe (ContentController.handleStaticResource - edit)
        request.getSession().setAttribute("templateHtml:" + template.getPath(), templateDocument.html());

        //will be used as server side state for this template
        request.getSession().setAttribute("templateDocument:" + template.getId(), templateDocument);
        request.getSession().setAttribute("templateElemCounter:" + template.getId(), templateElemCounter);

        //log as info
        Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Site: {0} Editing Template: {1}", new Object[]{site.getId(), template.getPath()});

        //return regular template obj
        model.addAttribute("template", template);
        return "admin/editTemplate";
    }

    //ajax - get template
    @RequestMapping()
    @ResponseBody
    public String getTemplate(@RequestParam Integer templateId, Site site) {
        Template template = contentService.getTemplate(templateId);
        if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", template);
    }

    //ajax - get template html original or current (for review changes)
    @RequestMapping()
    @ResponseBody
    public String getTemplateHtml(@RequestParam Integer templateId, @RequestParam Boolean original, HttpServletRequest request, Site site) {
        Template template = contentService.getTemplate(templateId);
        if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);

        //resulting template html
        String templateHtml = null;

        if (original) {
            //get the original unmodified html
            InputStream inputStream = null;
            try {
                Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
                inputStream = resourceService.getInputStream(toRootFolder(site), resource);
                templateHtml = IOUtils.toString(inputStream, "UTF-8");
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error getting the template resource for templateHtml", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template resource not found: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error reading template resource for templateHtml", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template resource not found: " + ex.getMessage(), null);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        } else {
            //current html
            Document templateDocument = (Document)request.getSession().getAttribute("templateDocument:" + templateId);
            if (templateDocument == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template document in session, please report this error to us", null);

            //clone the element (do not modify original - can save again), remove custom id and script, get html
            Document docCopy = templateDocument.clone();
            docCopy.getAllElements().removeAttr("fmgcms-id");
            docCopy.head().getElementById("fmgcms-injected-script").remove();
            templateHtml = docCopy.html();
        }

        //return the result
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", templateHtml);
    }

    //ajax - rename template
    @RequestMapping
    @ResponseBody
    public String renameTemplate(@RequestParam Integer templateId, @RequestParam String name, Site site) {
        Template template = contentService.getTemplate(templateId);
        if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);
        if (template.getName().equals(name)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "You did not change the name...", null);

        //change the name and save
        template.setName(name);
        contentService.saveTemplate(template);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Template is renamed successfully", null);
    }

    //ajax - get template element html
    @RequestMapping()
    @ResponseBody
    public String getTemplateElementHtml(@RequestParam int templateId, @RequestParam int elemId, HttpServletRequest request) {
        Document templateDocument = (Document)request.getSession().getAttribute("templateDocument:" + templateId);
        if (templateDocument == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template document in session, please report this error to us", null);
        Elements elements = templateDocument.getElementsByAttributeValue("fmgcms-id", "" + elemId);
        if (elements.isEmpty()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template element with this id, please report this error to us", null);

        //clone the element (do not modify original), remove custom id, return html
        Element element = elements.first().clone();
        element.getAllElements().removeAttr("fmgcms-id");
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", element.html());
    }

    //ajax - update template element html
    @RequestMapping()
    @ResponseBody
    public String updateTemplateElementHtml(@RequestParam int templateId, @RequestParam int elemId, @RequestParam String html, HttpServletRequest request) {
        Document templateDocument = (Document)request.getSession().getAttribute("templateDocument:" + templateId);
        if (templateDocument == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template document in session, please report this error to us", null);
        Elements elements = templateDocument.getElementsByAttributeValue("fmgcms-id", "" + elemId);
        if (elements.isEmpty()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template element with this id, please report this error to us", null);
        Integer templateElemCounter = (Integer)request.getSession().getAttribute("templateElemCounter:" + templateId);
        if (templateElemCounter == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template element index in session, please report this error to us", null);

        //update the element html
        Element element = elements.first();
        element.html(html);

        //regenerate custom attributes
        Elements allElements = element.getAllElements();
        for (Element elem : allElements) {
            if (elem.attr("fmgcms-id").equals(element.attr("fmgcms-id"))) continue; //skip self
            elem.attr("fmgcms-id", "" + templateElemCounter);
            templateElemCounter++;
        }
        request.getSession().setAttribute("templateElemMaxId:" + templateId, templateElemCounter);

        //return success, with the regenerated element html
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", element.html());
    }

    //ajax - make attribute
    @RequestMapping
    @ResponseBody
    public String saveTemplate(@RequestParam Integer templateId, @RequestParam String comment, @RequestParam Boolean publish, HttpServletRequest request, Site site) {
        Author user = (Author)request.getSession().getAttribute("user");
        Template template = contentService.getTemplate(templateId);
        if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);
        Document templateDocument = (Document)request.getSession().getAttribute("templateDocument:" + templateId);
        if (templateDocument == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template document in session, please report this error to us", null);

        //clone the element (do not modify original - can save again), remove custom id and script, get html
        Document docCopy = templateDocument.clone();
        docCopy.getAllElements().removeAttr("fmgcms-id");
        docCopy.head().getElementById("fmgcms-injected-script").remove();
        String templateHtml = docCopy.html();

        //add history record
        TemplateHistory templateHistory = new TemplateHistory();
        templateHistory.setTemplate(template);
        templateHistory.setHtml(templateHtml);
        templateHistory.setAuthor(user.getUsername());
        templateHistory.setComment(comment);
        templateHistory.setDate(new Date());
        contentService.saveTemplateHistory(templateHistory);

        //the result
        Map result = new HashMap();

        //update template html and version if this is not a draft
        if (publish) {
            //scan new attributes and add them to existing pages
            Set addedAttributes = scanAttributes(template, templateHtml, request.getParameterMap(), site);

            //save new template html to resource
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            if (resource == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Resource corresponding to this template is not found", null);
            OutputStream os = null;
            try {
                os = resourceService.getOutputStream(toRootFolder(site), resource);
                os.write(templateHtml.getBytes("UTF-8"));
                os.close();
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at saveTemplate write to resource os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at saveTemplate write to resource os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } finally {
                IOUtils.closeQuietly(os);
            }

            //process the new template html and save it as template file
            try {
                os = templateService.getSourceOutputStream(template.getId() + ".ftl");
                os.write(processTemplate(docCopy, resource, false, site).getBytes("UTF-8"));
                os.close();
            } catch (TemplateException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at saveTemplate write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at saveTemplate write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } finally {
                IOUtils.closeQuietly(os);
            }

            //set this version as the current version
            template.setVersion(templateHistory.getId());
            contentService.saveTemplate(template);

            //return updated objects if published (state change)
            result.put("template", template);
            result.put("templateHtml", templateHtml);
            result.put("addedAttributes", addedAttributes);
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Template is saved successfully. Following attributes are added;", result);
    }

    //ajax - template histories
    @RequestMapping()
    @ResponseBody
    public String getTemplateHistories(@RequestParam Integer templateId, Site site) {
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getTemplateHistories(templateId, site.getId()));
    }

    //ajax - revert template to history version
    @RequestMapping()
    @ResponseBody
    public String revertTemplate(@RequestParam Integer templateId, @RequestParam Integer templateHistoryId, @RequestParam Boolean publish, HttpServletRequest request, Site site) {
        Template template = contentService.getTemplate(templateId);
        TemplateHistory templateHistory = contentService.getTemplateHistory(templateHistoryId);
        if (template == null || templateHistory == null || !template.checkSite(site) || !templateHistory.getTemplate().checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);

        //the html to revert to
        String templateHtml = templateHistory.getHtml();

        //NOTE: this is kind of merge of editTemplate and saveTemplate (parts are copied from those blocks)
        //save the templateHtml as in saveTemplate
        //set the templateDocument as in editTemplate

        //>>>FROM EDITTEMPLATE>>>
        Document templateDocument = Jsoup.parse(templateHtml);
        templateDocument.outputSettings().prettyPrint(false);
        templateDocument.head().append("<script id=\"fmgcms-injected-script\" type=\"text/javascript\" src=\"" + request.getContextPath() + "/admin/js/firebug-lite/build/firebug-lite-debug.js\"></script>");
        int templateElemCounter = 0;
        Elements allElements = templateDocument.getAllElements();
        for (Element elem : allElements) {
            if (elem.tagName().equals("html")) continue; //do not directly edit html tag
            elem.attr("fmgcms-id", "" + templateElemCounter);
            templateElemCounter++;
        }

        //will be loaded by preview iframe (ContentController.handleStaticResource - edit)
        request.getSession().setAttribute("templateHtml:" + template.getPath(), templateDocument.html());

        //will be used as server side state for this template
        request.getSession().setAttribute("templateDocument:" + template.getId(), templateDocument);
        request.getSession().setAttribute("templateElemCounter:" + template.getId(), templateElemCounter);

        //>>>FROM SAVETEMPLATE>>>

        //the result
        Map result = new HashMap();
        if (publish) {
            //save new template html to resource
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            if (resource == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Resource corresponding to this template is not found", null);
            OutputStream os = null;
            try {
                os = resourceService.getOutputStream(toRootFolder(site), resource);
                os.write(templateHtml.getBytes("UTF-8"));
                os.close();
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at saveTemplate write to resource os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at saveTemplate write to resource os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } finally {
                IOUtils.closeQuietly(os);
            }

            //clone the element (do not modify original - can save again), remove custom id and script, get html
            Document docCopy = templateDocument.clone();
            docCopy.getAllElements().removeAttr("fmgcms-id");
            docCopy.head().getElementById("fmgcms-injected-script").remove();

            //process the new template html and save it as template file
            try {
                os = templateService.getSourceOutputStream(template.getId() + ".ftl");
                os.write(processTemplate(docCopy, resource, false, site).getBytes("UTF-8"));
                os.close();
            } catch (TemplateException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at saveTemplate write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at saveTemplate write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not save template, error: " + ex.getMessage(), null);
            } finally {
                IOUtils.closeQuietly(os);
            }

            //set this version as the current version
            template.setVersion(templateHistory.getId());
            contentService.saveTemplate(template);

            //return updated objects if published (state change)
            result.put("template", template);
            result.put("templateHtml", templateHtml);
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Template is reverted successfully", result);
    }

    //ajax - remove template history
    @RequestMapping()
    @ResponseBody
    public String removeTemplateHistory(@RequestParam Integer templateHistoryId, Site site) {
        TemplateHistory templateHistory = contentService.getTemplateHistory(templateHistoryId);
        if (templateHistory == null || !templateHistory.getTemplate().checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template History not found", null);
        contentService.removeTemplateHistory(templateHistoryId);
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Template History is removed successfully", null);
    }

    //--------------------------------------------------------------------------
    //ADVANCED
    //--------------------------------------------------------------------------
    //generate sitemap xml
    @RequestMapping()
    @ResponseBody()
    public String generateSitemap(HttpServletRequest request, HttpServletResponse response, Site site) throws IOException {
        //get the http://something part
        String urlPrefix = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length());
        response.setContentType("text/xml");

        StringBuffer sitemapBuffer = new StringBuffer();
        sitemapBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        //add all pages with last modified date
        List<Page> pages = contentService.getPages(site.getId());
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

    //--------------------------------------------------------------------------
    //ACCOUNT
    //--------------------------------------------------------------------------
    //manage account, sites & authors
    @RequestMapping()
    public String account(Model model, HttpServletRequest request, Site site) {
        Author user = (Author)request.getSession().getAttribute("user");
        Account account = accountService.getAccount(user.getAccount().getId()); //get the latest data, session might be old
        model.addAttribute("account", account);
        return "admin/account";
    }

    //view/edit profile
    @RequestMapping()
    public String profile(Model model, HttpServletRequest request, Site site) {
        model.addAttribute("author", request.getSession().getAttribute("user"));
        return "admin/profile";
    }

    /**
     * Inject editor code to page html
     * @param pageHtml the page html
     * @return injected page html
     */
    public static String injectEditor(String pageHtml, String contextPath) {
        String editorScript = ""
                + "<script type=\"text/javascript\" src=\"" + contextPath + "/admin/js/jquery-ui-1.8.20.custom.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"" + contextPath + "/admin/js/aloha/lib/aloha.js\" data-aloha-plugins=\"common/format,\n"
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
                + "<script type=\"text/javascript\" src=\"" + contextPath + "/admin/js/make-editable.js\"></script>\n"
                + "<link href=\"" + contextPath + "/admin/js/jquery-ui-base/jquery-ui-1.8.20.custom.css\" type=\"text/css\" rel=\"stylesheet\" />\n"
                + "<link href=\"" + contextPath + "/admin/js/aloha/css/aloha.css\" type=\"text/css\" rel=\"stylesheet\" />\n";

        //inject editor code
        StringBuffer pageBuffer = new StringBuffer(pageHtml);
        pageBuffer.insert(pageBuffer.indexOf("</head>"), editorScript);

        //replace placeholders with editable
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}"); //lazy match, to match multiple attrs in one line
        Matcher matcher = pattern.matcher(pageBuffer);
        matcher.region(pageBuffer.indexOf("<body>"), pageBuffer.length()); //do not inject to invisible region (head)
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            if (attribute.startsWith("i_")) continue; //do not edit invisible attributes
            matcher.appendReplacement(sb, "<div id=\"attribute-editable-$1\" onclick=\"window.parent.onAlohaClick('$1')\" class=\"editable\" style=\"display:inline-block\">\\${$1}</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //scan page attributes
    private void scanAttributes(Page page, Site site) {
        //scan the page template for ${} placeholders
        //detect the new page attributes, and add them to the page

        //new page, template not selected yet (this shouldn't happen anymore, but be safe)
        if (page.getTemplate() == null) return;

        //get the page's template html
        String templateHtml;
        InputStream inputStream = null;
        try {
            String resourcePath = page.getTemplate().getPath();
            if (resourcePath.equals("/")) resourcePath = "/index.html";
            Resource resource = resourceService.getResource(toRootFolder(site), resourcePath);
            inputStream = resourceService.getInputStream(toRootFolder(site), resource);
            templateHtml = IOUtils.toString(inputStream, "UTF-8");
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error getting the template resource for templateHtml", ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error reading template resource for templateHtml", ex);
            return;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        //scan template html for attributes
        Set<String> pageAttributes = new HashSet();
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(templateHtml);
        while (matcher.find()) {
            String attribute = matcher.group(1);
            pageAttributes.add(attribute);
        }

        //detect new attributes
        List<PageAttribute> existingPageAttributes = page.getPageAttributes();
        for (PageAttribute existingAttr : existingPageAttributes) {
            if (pageAttributes.contains(existingAttr.getAttribute())) pageAttributes.remove(existingAttr.getAttribute());
        }

        //add new attributes to the page
        for (String attribute : pageAttributes) {
            PageAttribute pageAttribute = new PageAttribute();
            pageAttribute.setAttribute(attribute);
            pageAttribute.setAuthor("admin");
            pageAttribute.setComment("attribute is scanned");
            pageAttribute.setDate(new Date());
            pageAttribute.setPage(page);
            pageAttribute.setValue("[Add Content]");
            pageAttribute.setVersion(0);
            contentService.savePageAttribute(pageAttribute);

            //add history record
            PageAttributeHistory attributeHistory = new PageAttributeHistory();
            attributeHistory.setPage(page);
            attributeHistory.setAttribute(pageAttribute.getAttribute());
            attributeHistory.setValue(pageAttribute.getValue());
            attributeHistory.setAuthor(pageAttribute.getAuthor());
            attributeHistory.setDate(pageAttribute.getDate());
            attributeHistory.setComment(pageAttribute.getComment());
            contentService.savePageAttributeHistory(attributeHistory);
        }
    }

    //scan template attributes and add to all pages
    private Set<String> scanAttributes(Template template, String templateHtml, Map attributeValues, Site site) {
        //scan the template for ${} placeholders
        //detect the new page attributes, and add them to the pages of the template

        //get the page's template html
        String originalTemplateHtml;
        InputStream inputStream = null;
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            inputStream = resourceService.getInputStream(toRootFolder(site), resource);
            originalTemplateHtml = IOUtils.toString(inputStream, "UTF-8");
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error getting the template resource for originalTemplateHtml", ex);
            return new HashSet();
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error reading template resource for originalTemplateHtml", ex);
            return new HashSet();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }


        //scan template html for attributes
        Set<String> pageAttributes = new HashSet();
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(templateHtml);
        while (matcher.find()) {
            String attribute = matcher.group(1);
            pageAttributes.add(attribute);
        }

        //scan original template html for attributes
        Set<String> existingPageAttributes = new HashSet();
        matcher = pattern.matcher(originalTemplateHtml);
        while (matcher.find()) {
            String attr = matcher.group(1);
            existingPageAttributes.add(attr);
        }

        //detect new attributes
        for (String existingAttr : existingPageAttributes) {
            if (pageAttributes.contains(existingAttr)) pageAttributes.remove(existingAttr);
        }

        //the pages of this template
        List<Page> pages = contentService.getPages(template.getId(), site.getId());

        //add new attributes to all pages of this template
        for (Page page : pages) {
            for (String attribute : pageAttributes) {
                PageAttribute pageAttribute = new PageAttribute();
                pageAttribute.setAttribute(attribute);
                pageAttribute.setAuthor("admin");
                pageAttribute.setComment("attribute is added to template");
                pageAttribute.setDate(new Date());
                pageAttribute.setPage(page);
                String[] defaultValue = (String[])attributeValues.get("newAttributes[" + attribute + "]");
                pageAttribute.setValue(defaultValue == null ? "[Add Content]" : defaultValue[0]);
                pageAttribute.setVersion(0);
                contentService.savePageAttribute(pageAttribute);

                //add history record
                PageAttributeHistory attributeHistory = new PageAttributeHistory();
                attributeHistory.setPage(page);
                attributeHistory.setAttribute(pageAttribute.getAttribute());
                attributeHistory.setValue(pageAttribute.getValue());
                attributeHistory.setAuthor(pageAttribute.getAuthor());
                attributeHistory.setDate(pageAttribute.getDate());
                attributeHistory.setComment(pageAttribute.getComment());
                contentService.savePageAttributeHistory(attributeHistory);
            }
        }

        return pageAttributes;
    }

    //process template - make links absolute
    private String processTemplate(Document templateDocument, Resource templateResource, boolean initial, Site site) {
        String contextPath = ""; //TODO & NOTE & IMPORTANT: We assume that the app does not have a contextPath, this won't work otherwise

        //process all links, media, imports
        //convert links to absolute path like /a/b/c
        Elements links = templateDocument.select("a[href]");
        Elements media = templateDocument.select("[src]");
        Elements imports = templateDocument.select("link[href]");
        Elements styles = templateDocument.select("style");

        //make media src absolute
        for (Element src : media) {
            src.attr("src", contextPath + templateResource.getFolder() + src.attr("src"));
        }

        //make css href absolute
        for (Element link : imports) {
            if (initial) processCss(contextPath, templateResource, resourceService.getResource(toRootFolder(site), templateResource.getFolder() + link.attr("href")), site); //if initial also update css contents
            link.attr("href", contextPath + templateResource.getFolder() + link.attr("href"));
        }

        //make link href absolute
        for (Element link : links) {
            if (!link.absUrl("href").equals("")) continue; //skip absolute links
            link.attr("href", contextPath + templateResource.getFolder() + link.attr("href"));
        }

        //also replace inline styles
        for (Element style : styles) {
            String cssContents = style.html();

            //process css contents (copied from processCss)
            Pattern pattern = Pattern.compile("url\\(\\s*(['\\\"]?+)(.*?)\\1\\s*\\)");
            Matcher matcher = pattern.matcher(cssContents);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "url('" + contextPath + templateResource.getFolder() + "$2')");
            }
            matcher.appendTail(sb);

            //replace inline style
            style.html(sb.toString());
        }

        return templateDocument.html();
    }

    //process css - make links absolute
    private void processCss(String contextPath, Resource templateResource, Resource cssResource, Site site) {
        //no such css
        if (cssResource == null) return;

        //copy the resource html as template file
        InputStream is = null;
        OutputStream os = null;
        String cssContents;
        try {
            is = resourceService.getInputStream(toRootFolder(site), cssResource);
            cssContents = IOUtils.toString(is, "UTF-8");

            //process css contents
            Pattern pattern = Pattern.compile("url\\(\\s*(['\\\"]?+)(.*?)\\1\\s*\\)");
            Matcher matcher = pattern.matcher(cssContents);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String cssUrl = matcher.group(2);
                if (cssUrl.startsWith("/") || cssUrl.startsWith("http")) continue; //skip absoulte urls
                matcher.appendReplacement(sb, "url('" + contextPath + cssResource.getFolder() + "$2')");
            }
            matcher.appendTail(sb);

            //write to the resource
            os = resourceService.getOutputStream(toRootFolder(site), cssResource);
            os.write(sb.toString().getBytes("UTF-8"));
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Resource exception while processing css contents", ex);
        } catch (IOException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException while processing css contents", ex);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    //convert user site to root folder (for resources)
    private String toRootFolder(Site site) {
        return "/" + site.getId();
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
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }
}
