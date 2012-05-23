/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.resource;

import com.fmguler.cms.service.resource.domain.Resource;
import java.io.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles static resource operations. Virtual folders for users' static files.
 * <p>
 * This implementation uses file system. But it can be converted to
 * Storage Service and/or S3.
 *
 * @author Fatih Mehmet Güler
 */
public class ResourceServiceImpl implements ResourceService {
    private String rootFolder;

    @Override
    public Resource getResource(String resourcePath) throws ResourceException {
        File file = new File(rootFolder, resourcePath);
        if (!file.exists()) return null;
        if (file.isDirectory()) return null;
        return fileToResource(file);
    }

    @Override
    public List getResources(String resourceFolder) throws ResourceException {
        List result = new LinkedList();

        File folder = new File(rootFolder, resourceFolder);
        if (!folder.exists()) throw new ResourceException(ResourceException.ERROR_FOLDER_NOT_FOUND, resourceFolder, null);
        if (!folder.isDirectory()) throw new ResourceException(ResourceException.ERROR_FOLDER_NOT_FOUND, resourceFolder, null);
        File[] files = folder.listFiles();
        if (files == null) throw new ResourceException(ResourceException.ERROR_FOLDER_NOT_FOUND, resourceFolder, null);

        //convert to service domain object
        for (File file : files) {
            result.add(fileToResource(file));
        }

        return result;
    }

    @Override
    public InputStream getInputStream(String resourcePath) throws ResourceException {
        try {
            File file = new File(rootFolder, resourcePath);
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resourcePath, ex);
        }
    }

    //file to resource
    private Resource fileToResource(File file) throws ResourceException {
        try {
            //make sure that path cannot go out of root folder using ..\
            String canonicalPath = file.getCanonicalPath();
            String resourceFolder = canonicalPath.substring(rootFolder.length(), canonicalPath.length() - file.getName().length());
            resourceFolder = resourceFolder.replaceAll("\\\\", "/"); // I hate back slashes

            //make resource object from file
            Resource resource = new Resource();
            resource.setFolder(resourceFolder);
            resource.setName(file.getName());
            resource.setContentLength((int)file.length());
            resource.setLastModified(new Date(file.lastModified()));
            resource.setDirectory(file.isDirectory());
            return resource;
        } catch (IOException ex) {
            throw new ResourceException(ResourceException.ERROR_UNKNOWN, ex); //file.canonicalpath throws exception, dont know why
        } catch (StringIndexOutOfBoundsException ex) {
            throw new ResourceException(ResourceException.ERROR_FOLDER_NOT_FOUND, "", ex); //trying to go out of root folder
        }
    }

    //--------------------------------------------------------------------------
    //SETTERS
    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }
}
