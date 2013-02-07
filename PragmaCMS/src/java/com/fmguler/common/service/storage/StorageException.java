/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service.storage;

import com.fmguler.common.service.ServiceException;

/**
 * Represents each error at Storage Service.
 * <p>
 * Errors are differentiated by error codes.
 * @author Fatih Mehmet Güler
 */
public class StorageException extends ServiceException {
    //ERROR CODES
    public static final String ERROR_INVALID_KEY = "ss-invalid-key"; //Invalid key (not hex, smaller than 5)
    public static final String ERROR_KEY_NOT_FOUND = "ss-key-not-found";
    public static final String ERROR_KEY_ALREADY_EXISTS = "ss-key-already-exists";
    public static final String ERROR_WRITE_FAILED = "ss-write-failed";
    public static final String ERROR_READ_FAILED = "ss-read-failed";

    /**
     * New StorageException with an error code
     */
    public StorageException(String errorCode) {
        super(errorCode);
    }

    /**
     * New StorageException with an error code and a cause
     */
    public StorageException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    /**
     * New StorageException with an error code, error parameter and a cause
     */
    public StorageException(String errorCode, String errorParam, Throwable cause) {
        super(errorCode, errorParam, cause);
    }

    @Override
    protected String getMessagePrefix() {
        return "Storage Exception";
    }
}
