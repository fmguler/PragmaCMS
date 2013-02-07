/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.resource;

import com.fmguler.cms.service.resource.domain.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Handles static resource operations. Virtual folders for users' static files.
 *
 * @author Fatih Mehmet Güler
 */
public interface ResourceService {
    /**
     * @param rootFolder the root folder resourcePath is relative to (not
     * included in resulting resource folder)
     * @param resourcePath the path of the resource
     * @return return the resource with the specified path, null if not exists
     */
    Resource getResource(String rootFolder, String resourcePath);

    /**
     * @param rootFolder the root folder resourcePath is relative to (not
     * included in resulting resources' folder)
     * @param folder the folder to list
     * @return the list of resources at the specified folder
     * @throws ResourceException folder does not exist
     */
    List getResources(String rootFolder, Resource folder) throws ResourceException;

    /**
     * Add folder with specified path
     * @param rootFolder the root folder resource is relative to
     * @param parentFolder parent folder
     * @param folderName the name of the folder
     * @throws ResourceException already exists or io error
     */
    void addFolder(String rootFolder, Resource parentFolder, String folderName) throws ResourceException;

    /**
     * Remove a file or a folder
     * @param rootFolder the root folder resource is relative to
     * @param resource the resource to remove
     * @throws ResourceException file not exists or io error
     */
    void removeResource(String rootFolder, Resource resource) throws ResourceException;

    /**
     * @param rootFolder the root folder resource is relative to
     * @param resource the resource to be read
     * @return input stream to the resource
     * @throws ResourceException resource does not exist
     */
    InputStream getInputStream(String rootFolder, Resource resource) throws ResourceException;

    /**
     * @param rootFolder the root folder resource is relative to
     * @param resource the resource to be written
     * @return output stream to the resource
     * @throws ResourceException io error
     */
    OutputStream getOutputStream(String rootFolder, Resource resource) throws ResourceException;

    /**
     * Extract existing zip file
     * @param rootFolder the root folder zipResource is relative to
     * @param zipResource the zip file
     * @param createFolder extract to named folder
     * @param removeZip remove the zip after extraction
     * @throws ResourceException zip file not exists or io error
     */
    void extractZip(String rootFolder, Resource zipResource, boolean createFolder, boolean removeZip) throws ResourceException;

    /**
     * Crawl a web page with all its resources to the specified folder
     *
     * @param rootFolder the root folder resource is relative to
     * @param parentFolder parent folder for all resources
     * @param pageUrl the url of the page to be crawled
     * @param followLinks follow links and crawl other same domain pages
     * @throws ResourceException
     */
    void crawlWebPage(String rootFolder, Resource parentFolder, String pageUrl, boolean followLinks) throws ResourceException;
}
