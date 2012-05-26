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
     * @param resourcePath the path of the resource
     * @return return the resource with the specified path, null if not exists
     */
    Resource getResource(String resourcePath);

    /**
     * @param resourceFolder the folder to list
     * @return the list of resources at the specified folder
     * @throws ResourceException folder does not exist
     */
    List getResources(String resourceFolder) throws ResourceException;

    /**
     * Add folder with specified path
     * @param resourceFolder the path of the folder, all folders will be created
     * @throws ResourceException already exists or io error
     */
    void addFolder(String resourceFolder) throws ResourceException;

    /**
     * Remove a file or a folder
     * @param resourcePath the path of the resource
     * @throws ResourceException file not exists or io error
     */
    void removeResource(String resourcePath) throws ResourceException;

    /**
     * @param resourcePath the path of the resource
     * @return input stream to the resource
     * @throws ResourceException resource does not exist
     */
    InputStream getInputStream(String resourcePath) throws ResourceException;

    /**
     * @param resourcePath the path of the resource to be written
     * @return output stream to the resource
     * @throws ResourceException io error
     */
    OutputStream getOutputStream(String resourcePath) throws ResourceException;

    /**
     * Extract existing zip file
     * @param zipResourcePath the path of the zip file
     * @param createFolder extract to named folder
     * @param removeZip remove the zip after extraction
     * @throws ResourceException zip file not exists or io error
     */
    void extractZip(String zipResourcePath, boolean createFolder, boolean removeZip) throws ResourceException;

    /**
     * Crawl a web page with all its resources to the specified folder
     *
     * @param baseFolder base folder for all resources
     * @param pageUrl the url of the page to be crawled
     * @param followLinks follow links and crawl other same domain pages
     * @throws ResourceException
     */
    void crawlWebPage(String baseFolder, String pageUrl, boolean followLinks) throws ResourceException;
}
