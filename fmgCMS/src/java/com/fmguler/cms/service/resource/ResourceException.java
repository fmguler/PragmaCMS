/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.resource;

import com.fmguler.common.service.ServiceException;

/**
 * Resource Service Exception
 *
 * @author Fatih Mehmet Güler
 */
public class ResourceException extends ServiceException {
    public static final String ERROR_RESOURCE_NOT_FOUND = "rs-resource-not-found";
    public static final String ERROR_FOLDER_NOT_FOUND = "rs-folder-not-found";
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
