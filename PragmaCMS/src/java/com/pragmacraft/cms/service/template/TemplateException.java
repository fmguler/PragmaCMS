/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.template;

import com.pragmacraft.common.service.ServiceException;

/**
 * Template Service exception
 *
 * @author Fatih Mehmet Güler
 */
public class TemplateException extends ServiceException {
    public static final String ERROR_MERGE_FAILED = "ts-merge-failed";
    public static final String ERROR_MERGE_SOURCE_FAILED = "ts-merge-source-failed";
    public static final String ERROR_READ_SOURCE_FAILED = "ts-read-source-failed";
    public static final String ERROR_WRITE_SOURCE_FAILED = "ts-write-source-failed";
    public static final String ERROR_UNKNOWN = "ts-unknown";

    public TemplateException(String errorCode) {
        super(errorCode);
    }

    public TemplateException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public TemplateException(String errorCode, String errorParam, Throwable cause) {
        super(errorCode, errorParam, cause);
    }


}
