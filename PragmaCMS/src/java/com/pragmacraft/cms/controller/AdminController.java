/*
 *  PragmaCMS
 *  Copyright 2011 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.controller;

import com.pragmacraft.cms.helper.CommonController;
import com.pragmacraft.cms.service.account.AccountService;
import com.pragmacraft.cms.service.account.domain.Account;
import com.pragmacraft.cms.service.account.domain.Author;
import com.pragmacraft.cms.service.content.ContentService;
import com.pragmacraft.cms.service.content.domain.*;
import com.pragmacraft.cms.service.resource.ResourceException;
import com.pragmacraft.cms.service.resource.ResourceService;
import com.pragmacraft.cms.service.resource.domain.Resource;
import com.pragmacraft.cms.service.template.TemplateException;
import com.pragmacraft.cms.service.template.TemplateService;
import com.pragmacraft.common.service.storage.StorageException;
import com.pragmacraft.common.service.storage.StorageService;
import com.pragmacraft.common.service.storage.domain.StorageObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
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
 *
 * @author Fatih Mehmet Güler
 */
@Controller
public class AdminController {
    private static final String APP_ADMIN = "fmguler";
    private static List<AuthenticationToken> authenticationTokens = new LinkedList();
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
            String username = ServletRequestUtils.getStringParameter(request, "username", "").toLowerCase(Locale.ENGLISH);
            String password = ServletRequestUtils.getStringParameter(request, "password", "");

            //PragmaCMS admin can login as other users;
            boolean loginAs = false;
            if (username.startsWith(APP_ADMIN + ":")) {
                Author appAdmin = accountService.getAuthor(APP_ADMIN);
                if (appAdmin != null && appAdmin.checkPassword(password)) {
                    loginAs = true;
                    username = username.substring(username.indexOf(":") + 1);
                    Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Site: {0} ({1}) Logging as {2}", new Object[]{request.getServerName(), site.getId(), username});
                } else {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Site: {0} ({1}) Unsuccessful login as attempt: {2}", new Object[]{request.getServerName(), site.getId(), username});
                    String errrorMessage = "Username/password incorrect!";
                    model.addAttribute("errorMessage", errrorMessage);
                    return "admin/login";
                }
            }

            //check user existence & authentication
            user = accountService.getAuthor(username);
            if (user == null || !user.checkPassword(password) && !loginAs) {
                //return error message
                String errrorMessage = "Username/password incorrect!";
                model.addAttribute("errorMessage", errrorMessage);
                return "admin/login";
            }

            //generate authentication token instead of putting to session
            String authToken = generateAuthToken(user);

            //log as info
            Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Generated authentication token for user: {0}", new Object[]{user.getUsername()});

            //return back to the desired url
            String returnUrl = (String)request.getSession().getAttribute("returnUrl");
            if (returnUrl == null) returnUrl = "/admin/home?none";
            request.getSession().removeAttribute("returnUrl");

            //set the domain as default domain if user is not authorized to the current requested domain            
            String primaryDomain = user.getAccount().getSites().isEmpty() ? user.getUsername() + ".pragmacms.com" : contentService.getSite(((Site)(user.getAccount().getSites().get(0))).getId()).toDomainArray()[0];
            String domain = user.getAccount().checkSite(site.getId()) ? request.getServerName() : primaryDomain;
            return "redirect:http://" + domain + returnUrl + "&authToken=" + authToken;
        }

        return "admin/login";
    }

    //ajax - jsonp - check if logged in to this site
    @RequestMapping
    @ResponseBody
    public String checkLogin(HttpServletRequest request, HttpServletResponse response) {
        String result = request.getSession().getAttribute("user") == null ? CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Not logged in", null) : CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Logged in", null);
        result = "jsonCallback(" + result + ")";
        return result;
    }

    //ajax - switch to the site
    @RequestMapping
    @ResponseBody
    public String switchSite(HttpServletRequest request, @RequestParam int siteId) {
        Author user = (Author)request.getSession().getAttribute("user");

        //update user sites in case it is changed
        user = accountService.getAuthor(user.getUsername());

        //check authorization
        if (!user.getAccount().checkSite(siteId)) CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "You cannot switch to this site", null);

        //generate authentication token instead of putting to session
        String authToken = generateAuthToken(user);

        //log as info
        Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Generated authentication token for user: {0}", new Object[]{user.getUsername()});

        //return the desired url
        String domain = contentService.getSite(siteId).toDomainArray()[0];
        String result = "http://" + domain + "/admin/pages?switch&authToken=" + authToken;
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", result);
    }

    //logout the user
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
        if (page.getPath().equals("/")) page.setPath("/index.html");
        if (!page.getPath().substring(1).matches("[A-Za-z0-9\\-./]{0,255}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Page path should not include any special character. Valid characters are; <br/><li>Letters (a-z/A-Z)<br/><li>Numbers (0-9)<li>Dash (-)<li>Dot (.)", null);
        if (contentService.getPage(page.getPath(), site.getId()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A page with this path already exists", null);

        //save the page
        page.setLastModified(new Date());
        page.setSite(site);
        contentService.savePage(page);

        //to get page attributes and template
        page = contentService.getPage(page.getId());

        //scan and auto add attributes
        createPageAttributes(page, site);

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
    public String resources(Model model, @RequestParam(required = false) String resourceFolder, @RequestParam(required = false) String addFolder, @RequestParam(required = false) String duplicateResource, Site site) {
        try {
            //partition the add folder /a/b/c to /a/b and c
            if (addFolder != null && addFolder.contains("/")) {
                resourceFolder = addFolder.substring(0, addFolder.lastIndexOf("/"));
                addFolder = addFolder.substring(addFolder.lastIndexOf("/") + 1);
            }

            //called with duplicateResource=path param
            if (duplicateResource != null && duplicateResource.contains("/")) {
                resourceFolder = duplicateResource.substring(0, duplicateResource.lastIndexOf("/"));
                duplicateResource = duplicateResource.substring(duplicateResource.lastIndexOf("/") + 1);
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
            model.addAttribute("duplicateResourceParam", duplicateResource);
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

            //synchronize templates with resources
            synchronizeTemplates(site);

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
            resourceService.addFolder(toRootFolder(site), folder.toResourcePath(), name);

            //return success
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (ResourceException ex) {
            if (ex.getErrorCode().equals(ResourceException.ERROR_FOLDER_ALREADY_EXISTS)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Folder already exists", null);
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add folder: " + ex.getMessage(), null);
        }
    }

    //ajax - duplicate resource
    @RequestMapping
    @ResponseBody
    public String duplicateResource(@RequestParam String resourcePath, @RequestParam String newName, Site site) {
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), resourcePath);
            if (resource == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Resource does not exist", null);

            //check name for special chars
            if (!newName.matches("[\\w\\-. ]{0,100}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Name contains invalid characters", null);

            //set the duplicate resource, copy at the same folder
            Resource duplicateResource = new Resource();
            duplicateResource.setDirectory(resource.getDirectory());
            duplicateResource.setFolder(resource.getFolder());
            duplicateResource.setName(newName);

            //check if duplicateResource already exists
            if (resourceService.getResource(toRootFolder(site), duplicateResource.toResourcePath()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "A resource with this name already exists", null);

            //duplicate the resource
            resourceService.copyResource(toRootFolder(site), resource, duplicateResource);

            //create template for the new resource
            try {
                createTemplateFromResource(site, duplicateResource);
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at duplicateResource cannot read duplicate resource", ex);
            } catch (TemplateException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at duplicateResource cannot write to template source os", ex);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at duplicateResource cannot write to template source os", ex);
            }

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
    public String crawlWebPage(@RequestParam String baseFolder, @RequestParam String pageUrl, @RequestParam(required = false) String followLinks, final Site site) {
        try {
            Resource folder = resourceService.getResource(toRootFolder(site), baseFolder);
            if (folder == null || !folder.getDirectory()) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Base folder does not exist", null);

            //crawl web page, asynchronously
            resourceService.crawlWebPage(toRootFolder(site), folder, pageUrl, followLinks != null, new Callable() {
                //called when crawling finishes
                public Object call() throws Exception {
                    //synchronize templates with resources
                    synchronizeTemplates(site);
                    return true;
                }
            });

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

    //remove template
    @RequestMapping
    @ResponseBody
    public String removeTemplate(@RequestParam int templateId, Model model, Site site) {
        try {
            Template template = contentService.getTemplate(templateId);
            if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);
            contentService.removeTemplate(templateId);
            templateService.removeSource(templateId + ".ftl");

            //also remove the resource
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            if (resource != null) resourceService.removeResource(toRootFolder(site), resource);

            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This template could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //--------------------------------------------------------------------------
    //EDIT TEMPLATE
    //--------------------------------------------------------------------------
    //edit template by resource path, also create template if not exists (called from resources)
    @RequestMapping
    public String editTemplateOfResource(@RequestParam String path, Model model, Site site) {
        //check the resource
        Resource resource = resourceService.getResource(toRootFolder(site), path);
        if (resource == null || resource.getDirectory() || !(resource.getName().endsWith(".htm") || resource.getName().endsWith(".html"))) {
            model.addAttribute("errorMessage", "A resource with given path could not be found.");
            model.addAttribute("errorAction", "Go Back");
            model.addAttribute("errorActionUrl", "resources");
            return "admin/error";
        }

        //check the template
        Template template = contentService.getTemplate(path, site.getId());

        //no such template
        if (template == null) {
            try {
                template = createTemplateFromResource(site, resource);
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at editTemplate cannot read resource", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
            } catch (TemplateException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at editTemplate cannot write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
            } catch (IOException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at editTemplate cannot write to template source os", ex);
                return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Could not add template, error: " + ex.getMessage(), null);
            }
        }

        //redirect to editTemplate
        return "redirect:/admin/editTemplate?id=" + template.getId();
    }

    //edit a template
    @RequestMapping()
    public String editTemplate(@RequestParam int id, @RequestParam(required = false) String ofPage, Model model, HttpServletRequest request, Site site) {
        Template template;

        //if ofPage parameter is set, edit the template of this page (or resource)
        if (ofPage != null) {
            Page page = contentService.getPage(ofPage, site.getId()); //try page
            if (page == null) template = contentService.getTemplate(ofPage, site.getId()); //try template with this path
            else template = page.getTemplate();
        } else template = contentService.getTemplate(id);

        //no such template
        if (template == null || !template.checkSite(site)) {
            model.addAttribute("errorMessage", "This template does not exist. You should add the template first.");
            model.addAttribute("errorAction", "Add Template");
            model.addAttribute("errorActionUrl", "templates");
            return "admin/error";
        }

        //get the original unmodified html (scripts can modify DOM)
        String templateHtml = null;
        try {
            templateHtml = getOriginalTemplateHtml(site, template);
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

    //ajax - get template html original or current (for review changes)
    @RequestMapping()
    @ResponseBody
    public String getTemplateHtml(@RequestParam Integer templateId, @RequestParam Boolean original, HttpServletRequest request, Site site) {
        Template template = contentService.getTemplate(templateId);
        if (template == null || !template.checkSite(site)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Template not found", null);

        //current html
        Document templateDocument = (Document)request.getSession().getAttribute("templateDocument:" + templateId);
        if (templateDocument == null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Cannot find template document in session, please report this error to us", null);

        //clone the element (do not modify original - can save again), remove custom id and script, get html
        Document docCopy = templateDocument.clone();
        docCopy.getAllElements().removeAttr("fmgcms-id");
        docCopy.head().getElementById("fmgcms-injected-script").remove();
        String templateHtml = docCopy.html();

        //return template, templateHtml, and templateAttributes for ajax state
        Map result = new HashMap();
        result.put("templateHtml", templateHtml);

        //return these only on the first call (on ready) do not return on review changes
        if (original) {
            template.setSite(null); //to hide in page ajax
            result.put("template", template);
            result.put("templateAttributes", contentService.getTemplateAttributes(templateId));
        }

        //return the result
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", result);
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
            Map attributesInfo = scanAttributes(template, templateHtml, request.getParameterMap(), site);

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
            result.put("templateAttributes", contentService.getTemplateAttributes(templateId));
            result.put("attributesInfo", attributesInfo);
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "Template is saved successfully.", result);
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
            //scan new attributes and add them to existing pages
            Map attributesInfo = scanAttributes(template, templateHtml, request.getParameterMap(), site);

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
            result.put("templateAttributes", contentService.getTemplateAttributes(templateId));
            result.put("attributesInfo", attributesInfo);
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
    //signup form
    @RequestMapping()
    public String signup(Model model, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        if (user != null) return "admin/pages";
        return "admin/signup";
    }

    //ajax - signup
    @RequestMapping()
    @ResponseBody
    public String doSignup(HttpServletRequest request) {
        if (request.getSession().getAttribute("user") != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "You are already logged in.", null);
        if (!request.getMethod().equals("POST")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Are you crazy? Why are you even trying this?", null);

        //get all the fields
        String company = ServletRequestUtils.getStringParameter(request, "company", "");
        String address = ServletRequestUtils.getStringParameter(request, "address", "");
        String city = ServletRequestUtils.getStringParameter(request, "city", "");
        String state = ServletRequestUtils.getStringParameter(request, "state", "");
        String country = ServletRequestUtils.getStringParameter(request, "country", "");
        String phone = ServletRequestUtils.getStringParameter(request, "phone", "");
        String firstName = ServletRequestUtils.getStringParameter(request, "firstName", "");
        String lastName = ServletRequestUtils.getStringParameter(request, "lastName", "");
        String username = ServletRequestUtils.getStringParameter(request, "username", "").toLowerCase(Locale.ENGLISH);
        String password = ServletRequestUtils.getStringParameter(request, "password", "");
        String email = ServletRequestUtils.getStringParameter(request, "email", "");

        //collect all the errors
        String errorMessage = "";

        //the most important checks first
        if (!username.matches("[A-Za-z0-9-.]{3,100}")) errorMessage += "<li>Please enter a valid username, no special chars.</li>";
        else if (accountService.getAuthor(username) != null) errorMessage += "<li>This username is taken, please choose another one. If this is you, <a href=\"login\"><u>login here</u></a></li>";
        if (password.trim().isEmpty()) errorMessage += "<li>Please enter a password.</li>";

        //check required fields
        if (company.trim().isEmpty()) errorMessage += "<li>Please fill company field. You can enter anything.</li>";

        //return all of the error(s)
        if (!errorMessage.equals("")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, errorMessage, null);

        ////////////////////////////////////////////////////////////////////////
        //LET'S DO IT!!!
        ////////////////////////////////////////////////////////////////////////
        //add account
        Account account = new Account();
        account.setCompany(company);
        account.setAddress(address);
        account.setCity(city);
        account.setState(state);
        account.setCountry(country);
        account.setPhone(phone);
        account.setPrimaryContact(new Author());
        accountService.saveAccount(account);

        //add author
        Author author = new Author();
        author.setUsername(username);
        author.resetPassword(password);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setEmail(email);
        author.setAccount(account);
        accountService.saveAuthor(author);

        //we should save the account again unfortunately (aaargh!)
        account.setPrimaryContact(author);
        accountService.saveAccount(account);

        //finally add site
        Site site = new Site();
        site.setAccount(account);
        String specialDomain = username.replace(".", "-") + ".pragmacms.com"; //create a unique domain for this site
        site.setDomains(specialDomain);
        contentService.saveSite(site);

        //create new site resources (new folder and sample designs)
        createSiteResources(site);

        //update author so that it's account include site (yes, i could add site to account here, but i think it is not neat)
        author = accountService.getAuthor(author.getUsername());

        //generate authentication token so that user won't have to login        
        String authToken = generateAuthToken(author);

        //log as info
        Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Generated authentication token for user: {0}", new Object[]{author.getUsername()});

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", "http://" + specialDomain + "/admin/resources" + "?none&authToken=" + authToken);
    }

    //manage account, sites & authors
    @RequestMapping()
    public String account(Model model, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        Account account = accountService.getAccount(user.getAccount().getId()); //get the account with authors and primaryContact (session data does not have these)
        model.addAttribute("account", account);
        return "admin/account";
    }

    //remove account
    @RequestMapping
    @ResponseBody
    public String removeAccount(@RequestParam String confirmationCode, Model model, HttpServletRequest request) {
        try {
            Author user = (Author)request.getSession().getAttribute("user");
            if (!confirmationCode.equals("SWUvtwB7C2ApEr4EpD0d")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Please do not play with this function, it's not a toy!", null);

            //first, remove all sites
            Account account = accountService.getAccount(user.getAccount().getId());
            List<Site> sites = account.getSites();
            for (Site site : sites) {
                //remove the site with all of its pages, templates and resources
                contentService.removeSite(site.getId());
                //also remove the resource folder
                try {
                    resourceService.removeResource("", resourceService.getResource("", site.getId() + ""));
                } catch (ResourceException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, "Error removing root folder of the site to be deleted. Site id: " + site.getId(), ex);
                }
            }

            //finally remove the account
            accountService.removeAccount(account.getId());

            //destroy session. 
            request.getSession().invalidate();
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Site: {0} Account has been removed: {1}", new Object[]{request.getServerName(), account.getId()});

            //TODO: template files will not be removed, they have to be garbage collected somehow
            //TODO: we should also destroy all of the account sessions; session of other users, and sessions of other sites
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This account could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //ajax - get site
    @RequestMapping()
    @ResponseBody
    public String getSite(@RequestParam Integer siteId, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        if (siteId != null && !user.getAccount().checkSite(siteId)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Site not found", null); //some body else's site
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", contentService.getSite(siteId));
    }

    //ajax - save/add site
    @RequestMapping
    @ResponseBody
    public String saveSite(@RequestParam Integer id, @RequestParam String domains, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        if (id != null && !user.getAccount().checkSite(id)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Site not found", null); //some body else's site

        //check each domain for validity and existence
        String[] domainArray = domains.split("\\s+");
        for (int i = 0; i < domainArray.length; i++) {
            if (domainArray[i].trim().isEmpty()) continue;
            if (!domainArray[i].matches("[A-Za-z0-9-.]{3,1000}")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Does not look like a domain name, domain names cannot include special chars: " + domainArray[i], null);
            Integer existingSiteId = contentService.resolveSiteId(domainArray[i]);
            if (existingSiteId != null && !user.getAccount().checkSite(existingSiteId)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "The domain name(" + domainArray[i] + ") is already associated with another account. If you own this domain name, please contact us.", null); //some body else's domain
            if (existingSiteId != null && !existingSiteId.equals(id)) contentService.removeDomainFromSite(existingSiteId, domainArray[i]); //NOTE: this is a move operation, this domain name will be hosted by the current site from now on
        }

        //save the site (also registers new domain names with the site)
        Site site = new Site(id);
        site.setDomains(domains);
        site.setAccount(user.getAccount());
        contentService.saveSite(site);

        //if a new site is added update user session, create resource root folder
        if (id == null) {
            createSiteResources(site);
            request.getSession().setAttribute("user", accountService.getAuthor(user.getUsername()));
        }

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", site);
    }

    //remove site
    @RequestMapping
    @ResponseBody
    public String removeSite(@RequestParam Integer siteId, @RequestParam String confirmationCode, Model model, HttpServletRequest request) {
        try {
            Author user = (Author)request.getSession().getAttribute("user");
            if (siteId != null && !user.getAccount().checkSite(siteId)) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Site not found", null); //some body else's site
            if (!confirmationCode.equals("nROqS9RUPH7Yh9WXdN8P")) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Please do not play with this function, it's not a toy!", null);

            //remove the site with all of its pages, templates and resources
            contentService.removeSite(siteId);

            //also remove the resource folder
            try {
                resourceService.removeResource("", resourceService.getResource("", siteId + ""));
            } catch (ResourceException ex) {
                Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, "Error removing root folder of the site to be deleted. Site id: " + siteId, ex);
            }

            //update the user session (TODO: actually we have to update all users logged in with this account, but for now, they'll have to log out and log in)
            request.getSession().setAttribute("user", accountService.getAuthor(user.getUsername()));

            //TODO: template files will not be removed, they have to be garbage collected somehow
            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This site could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //ajax - get author
    @RequestMapping()
    @ResponseBody
    public String getAuthor(@RequestParam Integer authorId, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        Author author = accountService.getAuthor(authorId);
        if (author == null || !user.getAccount().getId().equals(author.getAccount().getId())) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Author not found", null);

        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", author);
    }

    //ajax - save/add author
    @RequestMapping
    @ResponseBody
    public String saveAuthor(Author authorForm, HttpServletRequest request) {
        Author user = (Author)request.getSession().getAttribute("user");
        Author author = authorForm.getId() == null ? new Author() : accountService.getAuthor(authorForm.getId());
        if (authorForm.getId() != null && !user.getAccount().getId().equals(author.getAccount().getId())) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Author not found", null);
        if (!authorForm.getUsername().equals(author.getUsername()) && accountService.getAuthor(authorForm.getUsername()) != null) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This username is taken, please choose another one", null);

        //fields to be updated
        author.setUsername(authorForm.getUsername());
        author.setEmail(authorForm.getEmail());
        author.setFirstName(authorForm.getFirstName());
        author.setLastName(authorForm.getLastName());
        author.setAccount(user.getAccount());
        if (!authorForm.getPassword().trim().equals("")) author.resetPassword(authorForm.getPassword());
        accountService.saveAuthor(author);

        //return success
        return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", author);
    }

    //remove author
    @RequestMapping
    @ResponseBody
    public String removeAuthor(@RequestParam Integer authorId, Model model, HttpServletRequest request) {
        try {
            Author user = (Author)request.getSession().getAttribute("user");
            Author author = accountService.getAuthor(authorId);
            if (author == null || !user.getAccount().getId().equals(author.getAccount().getId())) return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "Author not found", null);

            //remove the author
            accountService.removeAuthor(authorId);

            return CommonController.toStatusJson(CommonController.JSON_STATUS_SUCCESS, "", null);
        } catch (Exception ex) { //TODO: service exception
            return CommonController.toStatusJson(CommonController.JSON_STATUS_FAIL, "This author could not be removed. Error: " + ex.getMessage(), null);
        }
    }

    //view/edit profile
    @RequestMapping()
    public String profile(Model model, HttpServletRequest request, Site site) {
        model.addAttribute("author", request.getSession().getAttribute("user"));
        return "admin/profile";
    }

    /**
     * Inject editor code to page html
     *
     * @param pageHtml the page html
     * @return injected page html
     */
    public static String injectEditor(String pageHtml, String contextPath) {
        String editorScript = ""
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
                + "<link href=\"" + contextPath + "/admin/js/aloha/css/aloha.css\" type=\"text/css\" rel=\"stylesheet\" />\n";

        //inject editor code
        StringBuffer pageBuffer = new StringBuffer(pageHtml);
        int beforeHead = pageBuffer.indexOf("</head>");

        //actually this will never happen because all templates are processed with jsoup which adds the missing tags, but be safe.
        if (beforeHead == -1) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Error injecting editor, pageHtml does not contain </head> tag");
            pageBuffer.insert(0, "<!-- Error: page does not contain </head> tag, editor won't work -->");
            return pageBuffer.toString();
        } else {
            pageBuffer.insert(beforeHead, editorScript);
        }

        //replace placeholders with editable
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}"); //lazy match, to match multiple attrs in one line
        Matcher matcher = pattern.matcher(pageBuffer);
        matcher.region(beforeHead + editorScript.length(), pageBuffer.length()); //do not inject to invisible region (head)
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            if (attribute.startsWith("i_")) continue; //do not edit invisible attributes
            matcher.appendReplacement(sb, "<div id=\"attribute-editable-$1\" onclick=\"window.parent.onAlohaClick('$1')\" class=\"editable\" style=\"display:inline-block\">\\${$1}</div>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Generate authentication token for the user.
     * This auth token can be used to get the user.
     *
     * @param user
     * @return auth token
     */
    public static String generateAuthToken(Author user) {
        AuthenticationToken token = new AuthenticationToken();
        token.expirationTime = new Date(new Date().getTime() + 5 * 60 * 1000);
        token.user = user;
        token.token = UUID.randomUUID().toString();
        authenticationTokens.add(token);
        return token.token;
    }

    /**
     * Return user by authentication token.
     * The token is invalidated.
     *
     * @param authToken the token generated by generate token
     * @return the user obj or null
     */
    public static Author getUserByAuthToken(String authToken) {
        Date now = new Date();
        Author result = null;
        Iterator<AuthenticationToken> it = authenticationTokens.iterator();
        while (it.hasNext()) {
            AuthenticationToken token = it.next();
            if (token.expirationTime.before(now)) it.remove();  //poor man's scheduler
            if (authToken != null && token.token.equals(authToken)) {
                result = token.user;
                it.remove(); //this token is consumed
            }
        }
        return result;
    }

    //PRIVATE
    //--------------------------------------------------------------------------
    //scan attributes from html
    private Set<String> scanAttributesCommon(String html) {
        Set<String> attributes = new HashSet();
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String attribute = matcher.group(1);
            attributes.add(attribute);
        }

        return attributes;
    }

    //scan page attributes
    private void createPageAttributes(Page page, Site site) {

        //new page, template not selected yet (this shouldn't happen anymore, but be safe)
        if (page.getTemplate() == null) return;

        //template attributes (current attributes)
        List<TemplateAttribute> templateAttributes = contentService.getTemplateAttributes(page.getTemplate().getId());

        //ad-hoc resource folder - instead of getting resource by templatePath and getting folder from it
        String templateResourceFolder = page.getTemplate().getPath().substring(0, page.getTemplate().getPath().lastIndexOf("/")) + "/";

        //add new attributes to the page
        for (TemplateAttribute templateAttribute : templateAttributes) {
            PageAttribute pageAttribute = new PageAttribute();
            pageAttribute.setAttribute(templateAttribute.getAttribute());
            pageAttribute.setAuthor("admin");
            pageAttribute.setComment("initial creation");
            pageAttribute.setDate(new Date());
            pageAttribute.setPage(page);
            pageAttribute.setValue(processAttribute(templateAttribute.getValue(), templateResourceFolder)); //default value
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
    private Map scanAttributes(Template template, String templateHtml, Map attributeValues, Site site) {
        //scan the template for ${} placeholders
        //detect the new page attributes, and add them to the pages of the template
        //return both new and all attributes

        Map result = new HashMap();

        //scan template html for attributes
        Set<String> newAttributes = scanAttributesCommon(templateHtml);

        //get template attributes (current attributes)
        List<TemplateAttribute> templateAttributes = contentService.getTemplateAttributes(template.getId());

        //detect removed attributes
        Set<String> removedAttributes = new HashSet();

        //detect re-added attributes
        Set<String> reAddedAttributes = new HashSet();

        //detect new attributes
        for (TemplateAttribute templateAttribute : templateAttributes) {

            //check if this attribute is new 
            if (newAttributes.contains(templateAttribute.getAttribute())) {

                //existing attribute, remove from new
                newAttributes.remove(templateAttribute.getAttribute());

                //this attribute was removed and now re-added                
                if (templateAttribute.getRemoved()) {
                    reAddedAttributes.add(templateAttribute.getAttribute());
                    templateAttribute.setTemplate(template);
                    String[] defaultValue = (String[])attributeValues.get("newAttributes[" + templateAttribute.getAttribute() + "]");
                    templateAttribute.setValue(defaultValue == null ? "" : defaultValue[0]);
                    templateAttribute.setRemoved(Boolean.FALSE);
                    contentService.saveTemplateAttribute(templateAttribute);
                }

            } else if (!templateAttribute.getRemoved()) {
                //this attribute does not exist in scanned attributes and not marked as removed
                //so this attribute is removed, mark as removed (don't remove in case it is re-added - not to lose data on existing pages)
                removedAttributes.add(templateAttribute.getAttribute());
                templateAttribute.setTemplate(template);
                templateAttribute.setRemoved(Boolean.TRUE);
                contentService.saveTemplateAttribute(templateAttribute);
            }
        }

        //add new attributes as template attributes (current attributes)
        for (String attribute : newAttributes) {
            TemplateAttribute templateAttribute = new TemplateAttribute();
            templateAttribute.setTemplate(template);
            templateAttribute.setAttribute(attribute);
            String[] defaultValue = (String[])attributeValues.get("newAttributes[" + attribute + "]");
            templateAttribute.setValue(defaultValue == null ? "" : defaultValue[0]);
            templateAttribute.setRemoved(Boolean.FALSE);
            contentService.saveTemplateAttribute(templateAttribute);
        }

        //the pages of this template
        List<Page> pages = contentService.getPages(template.getId(), site.getId());

        //ad-hoc resource folder - instead of getting resource by templatePath and getting folder from it
        String templateResourceFolder = template.getPath().substring(0, template.getPath().lastIndexOf("/")) + "/";

        //add new attributes to all pages of this template
        for (Page page : pages) {
            for (String attribute : newAttributes) {
                try {
                    PageAttribute pageAttribute = new PageAttribute();
                    pageAttribute.setAttribute(attribute);
                    pageAttribute.setAuthor("admin");
                    pageAttribute.setComment("attribute is added to template");
                    pageAttribute.setDate(new Date());
                    pageAttribute.setPage(page);
                    String[] defaultValue = (String[])attributeValues.get("newAttributes[" + attribute + "]");
                    pageAttribute.setValue(processAttribute(defaultValue == null ? "" : defaultValue[0], templateResourceFolder));
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
                } catch (RuntimeException e) {
                    //this should never ever happen since we handle re-added attributes above
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "Runtime exception at scanAttributes, this shouldn't have happened: {0}", e.getMessage());
                    throw (e);
                }
            }
        }

        //return new, removed and re-added attributes (for information purposes)
        result.put("addedAttributes", newAttributes);
        result.put("removedAttributes", removedAttributes);
        result.put("reAddedAttributes", reAddedAttributes);

        return result;
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

        //make media src absolute - img and script tags
        for (Element src : media) {
            if (skipURL(src.attr("src"))) continue; //skip absolute links
            src.attr("src", contextPath + templateResource.getFolder() + src.attr("src"));
        }

        //make css href absolute
        for (Element link : imports) {
            if (initial) processCss(contextPath, templateResource, resourceService.getResource(toRootFolder(site), templateResource.getFolder() + link.attr("href")), site); //if initial also update css contents
            if (skipURL(link.attr("href"))) continue; //skip absolute links
            link.attr("href", contextPath + templateResource.getFolder() + link.attr("href"));
        }

        //make link href absolute
        for (Element link : links) {
            if (skipURL(link.attr("href"))) continue; //skip absolute links
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

    //process attribute - make links absolute (for the html fragment) same as processTemplate but for given html fragment
    private String processAttribute(String html, String resourceFolder) {

        //the difference from processTemplate is we parse html here as fragment
        Document attributeDocument = Jsoup.parseBodyFragment(html);
        attributeDocument.outputSettings().prettyPrint(false);

        String contextPath = ""; //TODO & NOTE & IMPORTANT: We assume that the app does not have a contextPath, this won't work otherwise

        //process all links, media, imports
        //convert links to absolute path like /a/b/c
        Elements links = attributeDocument.select("a[href]");
        Elements media = attributeDocument.select("[src]");
        Elements imports = attributeDocument.select("link[href]");
        Elements styles = attributeDocument.select("style");

        //make media src absolute
        for (Element src : media) {
            src.attr("src", contextPath + resourceFolder + src.attr("src"));
        }

        //make css href absolute
        for (Element link : imports) {
            link.attr("href", contextPath + resourceFolder + link.attr("href"));
        }

        //make link href absolute
        for (Element link : links) {
            if (!link.absUrl("href").equals("")) continue; //skip absolute links
            link.attr("href", contextPath + resourceFolder + link.attr("href"));
        }

        //also replace inline styles
        for (Element style : styles) {
            String cssContents = style.html();

            //process css contents (copied from processCss)
            Pattern pattern = Pattern.compile("url\\(\\s*(['\\\"]?+)(.*?)\\1\\s*\\)");
            Matcher matcher = pattern.matcher(cssContents);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "url('" + contextPath + resourceFolder + "$2')");
            }
            matcher.appendTail(sb);

            //replace inline style
            style.html(sb.toString());
        }

        return attributeDocument.body().html();
    }

    //convert user site to site root folder (for resources)
    private String toRootFolder(Site site) {
        //return "/" + site.getId() + "/";
        return "/" + site.getId();
    }

    //create new site folder and add sample design templates
    private void createSiteResources(Site site) {
        try {
            //first, create the root folder for the resources
            resourceService.addFolder(ResourceService.ROOT_FOLDER, ResourceService.ROOT_FOLDER, site.getId() + "");

            //then copy some sample design templates
            String[] samples = new String[]{"softwaretemplate.zip"};
            for (int i = 0; i < samples.length; i++) {

                //copy the sample zip
                Resource sampleDesignSrc = resourceService.getResource(ResourceService.ROOT_FOLDER, "/sample/" + samples[i]);
                Resource sampleDesignDst = new Resource();
                sampleDesignDst.setFolder(toRootFolder(site) + "/");
                sampleDesignDst.setName(samples[i]);
                resourceService.copyResource(ResourceService.ROOT_FOLDER, sampleDesignSrc, sampleDesignDst);

                //finally extract and delete the zip.
                resourceService.extractZip(ResourceService.ROOT_FOLDER, sampleDesignDst, false, true);
            }
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.SEVERE, "Error creating root folder of the new site. Site id: " + site.getId(), ex);
        }

        //synchronize templates with resources
        synchronizeTemplates(site);
    }

    //create template from .html resource
    private Template createTemplateFromResource(Site site, Resource resource) throws ResourceException, TemplateException, IOException {
        //create template
        Template template = new Template();
        template.setName(resource.toResourcePath()); //TODO: not used anymore, delete later
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

            //add initial history record
            TemplateHistory templateHistory = new TemplateHistory();
            templateHistory.setTemplate(template);
            templateHistory.setHtml(templateHtml);
            templateHistory.setAuthor("admin");
            templateHistory.setComment("template is added");
            templateHistory.setDate(new Date());
            contentService.saveTemplateHistory(templateHistory);

            //set the published template version            
            template.setVersion(templateHistory.getId());
            contentService.saveTemplate(template);

            //check if resource has attributes (this shouldn't happen, but just in case)
            Set<String> attributes = scanAttributesCommon(templateHtml);
            for (String attribute : attributes) {
                TemplateAttribute scannedAttribute = new TemplateAttribute();
                scannedAttribute.setAttribute(attribute);
                scannedAttribute.setValue(""); //no default value since it was already in there
                scannedAttribute.setRemoved(Boolean.FALSE);
                scannedAttribute.setTemplate(template);
                contentService.saveTemplateAttribute(scannedAttribute);
                Logger.getLogger(AdminController.class.getName()).log(Level.INFO, "Initial resource contains scanned attribute: {0}", attribute);
            }

            //save processed form as template file
            Document templateDocument = Jsoup.parse(templateHtml);
            templateDocument.outputSettings().prettyPrint(false);
            os = templateService.getSourceOutputStream(template.getId() + ".ftl");
            os.write(processTemplate(templateDocument, resource, true, site).getBytes("UTF-8"));
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        return template;
    }

    //synchronize html resources and templates for the site
    private void synchronizeTemplates(Site site) {
        //get all the .html resources of the site;
        //check if a template already exists for this resource
        //if not add a template object and write as template source
        //detect extra templates without resource and delete? recover? them

        //all resources
        List<Resource> resources;
        try {
            resources = resourceService.getAllResources(toRootFolder(site));
        } catch (ResourceException ex) {
            Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at synchronizeTemplates cannot get all resources", ex);
            return;
        }

        //all templates
        List<Template> templates = contentService.getTemplates(site.getId());

        //iterate over resources
        for (Resource resource : resources) {
            if (!resource.getName().toLowerCase().matches(".*(.html|.htm)")) continue; //i know i could do with endsWith, but it was a matter of pride

            //search for template
            boolean hasTemplate = false;

            //poor man's n^2 search
            for (Template template : templates) {
                if (template.getPath().equals(resource.toResourcePath())) {
                    hasTemplate = true;
                    break;
                }
            }

            //create one if not exists
            if (!hasTemplate) {
                try {
                    createTemplateFromResource(site, resource);
                } catch (ResourceException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "ResourceException at synchronizeTemplates cannot read resource", ex);
                } catch (TemplateException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "TemplateException at synchronizeTemplates cannot write to template source os", ex);
                } catch (IOException ex) {
                    Logger.getLogger(AdminController.class.getName()).log(Level.WARNING, "IOException at synchronizeTemplates cannot write to template source os", ex);
                }
            }
        }
    }

    //get the original unmodified html of the template (from resource)
    private String getOriginalTemplateHtml(Site site, Template template) throws ResourceException, IOException {
        InputStream inputStream = null;
        String templateHtml = null;
        try {
            Resource resource = resourceService.getResource(toRootFolder(site), template.getPath());
            if (resource == null) throw new IOException("Resource of this template is not found.");
            inputStream = resourceService.getInputStream(toRootFolder(site), resource);
            templateHtml = IOUtils.toString(inputStream, "UTF-8");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return templateHtml;
    }

    //check if URL is to be skipped
    private boolean skipURL(String url) {
        try {
            //skip URL if it is absolute, starts with / or #
            URI u = new URI(url);
            if (u.isAbsolute()) return true;
            if (u.getPath().startsWith("/")) return true;
            if (u.getPath().equals("")) return true; //if it has only fragment (#abc) it should not have a path
            return false;
        } catch (Exception e) {
            return true; //don't try to make it relative if you cannot parse it
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

//simple auth token for cross domain sign on
class AuthenticationToken {
    public Author user;
    public String token;
    public Date expirationTime;
}
