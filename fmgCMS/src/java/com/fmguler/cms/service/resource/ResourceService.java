/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.resource;

import com.fmguler.cms.service.resource.domain.Resource;
import java.io.InputStream;
import java.util.List;

/**
 * Handles static resource operations. Virtual folders for users' static files.
 *
 * @author Fatih Mehmet Güler
 */
public interface ResourceService {

    /**
     * @param resourcePath the path of the resource
     * @return return the resource with the specified path
     * @throws ResourceException resource does not exist
     */
    Resource getResource(String resourcePath) throws ResourceException;

    /**
     * @param resourceFolder the folder to list
     * @return the list of resources at the specified folder
     * @throws ResourceException folder does not exist
     */
    List getResources(String resourceFolder) throws ResourceException;

    /**
     * @param resourcePath the path of the resource
     * @return input stream to the resource
     * @throws ResourceException resource does not exist
     */
    InputStream getInputStream(String resourcePath) throws ResourceException;
}
