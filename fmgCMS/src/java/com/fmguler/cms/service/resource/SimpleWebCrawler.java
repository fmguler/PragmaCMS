/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Simple web crawler sub-service for resource service.
 * <p>
 * Crawls web pages with all their resources and saves them to a folder.
 *
 * @author Fatih Mehmet Güler
 */
public class SimpleWebCrawler {
    private Set downloadedUrls = new HashSet();

    /**
     * Crawl a web page and save it to the folder
     * @param folder the folder where all resources will be saved to
     * @param pageUrl the absolute URL of the page to be crawled
     * @throws ResourceException any exception
     */
    public void crawl(File folder, String pageUrl, boolean followLinks) {
        try {
            System.out.println("Crawling Page: " + pageUrl);

            //save the page
            URL parentUrl = new URL(pageUrl);
            downloadUrl(folder, parentUrl, parentUrl, true);

            //parse the page html
            Document doc = Jsoup.connect(pageUrl).get();
            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            Elements imports = doc.select("link[href]");

            //all media elements, save them to the folder
            for (Element src : media) {
                URL url = new URL(src.attr("abs:src"));
                downloadUrl(folder, parentUrl, url, false);
            }

            //all css elements, save them to the folder and search them for resources like background: url('abc')
            for (Element link : imports) {
                URL url = new URL(link.attr("abs:href"));
                downloadUrl(folder, parentUrl, url, false);
                Pattern pattern = Pattern.compile("url\\(\\s*(['\\\"]?+)(.*?)\\1\\s*\\)");
                Matcher matcher = pattern.matcher(downloadUrlContents(url));
                while (matcher.find()) {
                    URL cssUrl = new URL(url, matcher.group(2));
                    downloadUrl(folder, parentUrl, cssUrl, false);
                }
            }

            //all links, not saving right now, but may be needed for full crawling
            for (Element link : links) {
                String urlStr = link.attr("abs:href");
                URL url = new URL(urlStr);
                if (url.getRef() != null) continue; //I hate anchors
                if (url.getQuery() != null) continue; //query strings are also evil
                if (followLinks && !downloadedUrls.contains(url) && sameDomain(parentUrl, url)) crawl(folder, urlStr, followLinks);
            }
        } catch (IOException ex) {
            Logger.getLogger(SimpleWebCrawler.class.getName()).log(Level.WARNING, "SKIPPING - Crawling page failed for url: {0} error: {1}", new Object[]{pageUrl, ex.getMessage()});
        }
    }

    //download the url to the folder
    private void downloadUrl(File folder, URL parentUrl, URL url, boolean page) {
        try {
            //do not redownload if already downloaded
            if (downloadedUrls.contains(url)) return;

            //add to downloaded list
            downloadedUrls.add(url);

            //never download from other domains - we do not want to download whole internet as internet.zip :)
            if (!sameDomain(parentUrl, url)) return;

            //get the file name, add .html to unnamed pages, or ones with .php, etc
            String fileName = url.getPath();
            if (fileName.isEmpty() || fileName.endsWith("/")) fileName += "index.html";
            //if (page && !fileName.endsWith(".htm") && !fileName.endsWith(".html")) fileName += ".html";

            //save to the file
            File file = new File(folder, fileName);
            FileUtils.copyURLToFile(url, file);
        } catch (IOException ex) {
            Logger.getLogger(SimpleWebCrawler.class.getName()).log(Level.WARNING, "SKIPPING - Crawling resource failed for url: {0} error: {1}", new Object[]{url, ex.getMessage()});
        }
    }

    //download url and return contents as string
    private String downloadUrlContents(URL url) {
        try {
            return IOUtils.toString(url);
        } catch (IOException ex) {
            Logger.getLogger(SimpleWebCrawler.class.getName()).log(Level.WARNING, "SKIPPING - Getting resource contents failed for url: {0} error: {1}", new Object[]{url, ex.getMessage()});
            return "";
        }
    }

    //check same domain
    private boolean sameDomain(URL parentUrl, URL url) {
        return url.getHost().equals(parentUrl.getHost());
    }
}
