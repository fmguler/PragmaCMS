/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.resource;

import com.pragmacraft.common.service.ServiceException;

/**
 * Resource Service Exception
 *
 * @author Fatih Mehmet Güler
 */
public class ResourceException extends ServiceException {
    public static final String ERROR_RESOURCE_NOT_FOUND = "rs-resource-not-found";
    public static final String ERROR_FOLDER_ALREADY_EXISTS = "rs-folder-exists";
    public static final String ERROR_EXTRACT_FAILED = "rs-extract-failed";
    public static final String ERROR_CRAWL_FAILED = "rs-crawl-failed";
    public static final String ERROR_UNKNOWN = "rs-unknown";

    public ResourceException(String errorCode) {
        super(errorCode);
    }

    public ResourceException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ResourceException(String errorCode, String errorParam, Throwable cause) {
        super(errorCode, errorParam, cause);
    }
}
