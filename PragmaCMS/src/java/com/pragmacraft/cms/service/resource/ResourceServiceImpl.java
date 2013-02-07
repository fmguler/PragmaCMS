/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.resource;

import com.pragmacraft.cms.service.resource.domain.Resource;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
    public Resource getResource(String rootFolder, String resourcePath) {
        try {
            File file = resourcePathToFile(rootFolder, resourcePath);
            if (!file.exists()) return null;
            return fileToResource(rootFolder, file);
        } catch (ResourceException ex) {
            Logger.getLogger(ResourceServiceImpl.class.getName()).log(Level.WARNING, "Unthrowed ResourceService error at getResource:" + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public List getResources(String rootFolder, Resource resourceFolder) throws ResourceException {
        List result = new LinkedList();

        File folder = resourcePathToFile(rootFolder, resourceFolder.toResourcePath()); //always check path again
        if (!folder.exists()) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resourceFolder.toResourcePath(), null);
        if (!folder.isDirectory()) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resourceFolder.toResourcePath(), null);
        File[] files = folder.listFiles();
        if (files == null) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resourceFolder.toResourcePath(), null);

        //sort according to names
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                return f1.getName().compareTo(f2.getName());
            }
        });

        //convert to service domain object
        for (File file : files) {
            result.add(fileToResource(rootFolder, file));
        }

        return result;
    }

    @Override
    public void addFolder(String resourceRootFolder, Resource parentFolder, String folderName) throws ResourceException {
        try {
            File folder = resourcePathToFile(resourceRootFolder, parentFolder.toResourcePath()); //always check path again
            File newFolder = new File(folder, folderName).getCanonicalFile();
            String rootCanonicalPath = new File(rootFolder + resourceRootFolder).getCanonicalPath();
            if (!newFolder.getCanonicalPath().startsWith(rootCanonicalPath)) throw new ResourceException(ResourceException.ERROR_UNKNOWN);
            if (newFolder.exists()) throw new ResourceException(ResourceException.ERROR_FOLDER_ALREADY_EXISTS);
            if (!newFolder.mkdirs()) throw new ResourceException(ResourceException.ERROR_UNKNOWN);
        } catch (IOException ex) {
            throw new ResourceException(ResourceException.ERROR_UNKNOWN, ex); //file.canonicalpath throws exception, dont know why
        }
    }

    @Override
    public void removeResource(String rootFolder, Resource resource) throws ResourceException {
        try {
            File file = resourcePathToFile(rootFolder, resource.toResourcePath()); //always check path again
            if (!file.exists()) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND);
            FileUtils.forceDelete(file);
        } catch (IOException ex) {
            throw new ResourceException(ResourceException.ERROR_UNKNOWN, ex);
        }
    }

    @Override
    public InputStream getInputStream(String rootFolder, Resource resource) throws ResourceException {
        try {
            File file = resourcePathToFile(rootFolder, resource.toResourcePath()); //always check path again
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resource.toResourcePath(), ex);
        }
    }

    @Override
    public OutputStream getOutputStream(String rootFolder, Resource resource) throws ResourceException {
        try {
            File file = resourcePathToFile(rootFolder, resource.toResourcePath()); //always check path again
            return new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND, resource.toResourcePath(), ex);
        }
    }

    @Override
    public void extractZip(String rootFolder, Resource zipResource, boolean createFolder, boolean removeZip) throws ResourceException {
        try {
            File zipFile = resourcePathToFile(rootFolder, zipResource.toResourcePath()); //always check path again
            FileInputStream inputStream = new FileInputStream(zipFile);

            //create a folder or extract to place
            File extractedFolder;
            if (createFolder) {
                extractedFolder = new File(zipFile.getParentFile(), zipFile.getName().substring(0, zipFile.getName().lastIndexOf('.')));
                extractedFolder.mkdir();
            } else extractedFolder = zipFile.getParentFile();

            //extract the zip
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File entryFile = new File(extractedFolder, entry.getName());
                if (entry.isDirectory()) entryFile.mkdirs();
                else {
                    FileOutputStream out = new FileOutputStream(entryFile);
                    IOUtils.copyLarge(zis, out);
                    IOUtils.closeQuietly(out);
                }
            }
            zis.close();

            //remove the zip file
            if (removeZip) {
                FileUtils.forceDelete(zipFile);
            }
        } catch (IOException ex) {
            throw new ResourceException(ResourceException.ERROR_EXTRACT_FAILED, ex);
        }
    }

    @Override
    public void crawlWebPage(String rootFolder, Resource parentFolder, final String pageUrl, final boolean followLinks) throws ResourceException {
        final File crawlFolder = resourcePathToFile(rootFolder, parentFolder.toResourcePath());
        new Thread(new Runnable() {
            public void run() {
                SimpleWebCrawler crawler = new SimpleWebCrawler();
                crawler.crawl(crawlFolder, pageUrl, followLinks);
                System.out.println("***************CRAWLING FINISHED FOR***************");
                System.out.println(pageUrl);
            }
        }).start();
    }

    //--------------------------------------------------------------------------
    //PRIVATE
    //--------------------------------------------------------------------------
    //file to resource
    private Resource fileToResource(String resourceRootFolder, File file) throws ResourceException {
        try {
            //make sure that path cannot go out of root folder using ..\
            file = file.getCanonicalFile();
            String canonicalPath = file.getCanonicalPath();
            String rootCanonicalPath = new File(rootFolder + resourceRootFolder).getCanonicalPath();
            if (!canonicalPath.startsWith(rootCanonicalPath)) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND);

            //special treatment for home folder /
            if (canonicalPath.equals(rootCanonicalPath)) {
                Resource resource = new Resource();
                resource.setFolder("");
                resource.setName("");
                resource.setContentLength(0);
                resource.setLastModified(new Date(file.lastModified()));
                resource.setDirectory(true);
                return resource;
            }

            //extract folder
            String resourceFolder = canonicalPath.substring(rootFolder.length() + resourceRootFolder.length(), canonicalPath.length() - file.getName().length());
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
        }
    }

    //resource path to file
    private File resourcePathToFile(String resourceRootFolder, String resourcePath) throws ResourceException {
        try {
            //make sure that path cannot go out of root folder using ..\
            if (resourcePath == null) resourcePath = "";
            if (resourceRootFolder == null) resourceRootFolder = "";
            File file = new File(rootFolder + resourceRootFolder, resourcePath);
            String canonicalPath = file.getCanonicalPath();
            String rootCanonicalPath = new File(rootFolder + resourceRootFolder).getCanonicalPath();
            if (!canonicalPath.startsWith(rootCanonicalPath)) throw new ResourceException(ResourceException.ERROR_RESOURCE_NOT_FOUND);
            return file;
        } catch (IOException ex) {
            throw new ResourceException(ResourceException.ERROR_UNKNOWN, ex); //file.canonicalpath throws exception, dont know why
        }
    }

    //--------------------------------------------------------------------------
    //SETTERS
    //--------------------------------------------------------------------------
    public void setRootFolder(String rootFolder) throws IOException {
        this.rootFolder = new File(rootFolder).getCanonicalPath();
    }
}
