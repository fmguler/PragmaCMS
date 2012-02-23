/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service;

/**
 * Represents each service error.
 * Service implementations can (should) extend this exception for their specific
 * exception classes.
 * <p>
 * Errors are differentiated by error codes.
 *
 * @author Fatih Mehmet Güler
 */
public class ServiceException extends Exception {
    private String errorCode;
    private String errorParam;

    /**
     * New ServiceException with an error code
     */
    public ServiceException(String errorCode) {
        super();
        if (errorCode == null) throw new IllegalArgumentException("ServiceException: errorCode cannot be null");
        this.errorCode = errorCode;
    }

    /**
     * New ServiceException with an error code and a cause
     */
    public ServiceException(String errorCode, Throwable cause) {
        super(cause);
        if (errorCode == null) throw new IllegalArgumentException("ServiceException: errorCode cannot be null");
        this.errorCode = errorCode;
    }

    /**
     * New ServiceException with an error code, error parameter and a cause
     */
    public ServiceException(String errorCode, String errorParam, Throwable cause) {
        super(cause);
        if (errorCode == null) throw new IllegalArgumentException("ServiceException: errorCode cannot be null");
        this.errorCode = errorCode;
        this.errorParam = errorParam;
    }

    /**
     * The message prefix to be outputted in getMessage()
     * <p>
     * Sub classes can override this method to customize the message prefix.
     */
    protected String getMessagePrefix() {
        return this.getClass().getName();
    }

    /**
     * Get the status code for this error.
     * <p>
     * Can be compared with EC_ error codes in this class.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get the parameter for this error.
     * Contains additional info / parameters in the error code
     */
    public String getErrorParam() {
        return errorParam;
    }

    @Override
    public String getMessage() {
        String message = getMessagePrefix() + ": " + errorCode;
        if (errorParam != null) message += " (" + errorParam + ")";
        return message;
    }

    /**
     * Adds a new error parameter.
     * If a parameter already exists, params will be seperated by comma.
     */
    public void addErrorParam(String errorParam) {
        if (this.errorParam == null || this.errorParam.trim().equals("")) {
            this.errorParam = errorParam;
        } else {
            this.errorParam += ", " + errorParam;
        }
    }

    /**
     * Returns the all causes of this exception
     */
    public String getAllCauses() {
        return getAllCauses(this);
    }

    /**
     * Returns the all causes of this exception
     */
    public static String getAllCauses(Exception ex) {
        Throwable currentCause = ex.getCause();
        StringBuffer causeBuffer = new StringBuffer();
        while (currentCause != null) {
            causeBuffer.append(currentCause.toString());
            currentCause = currentCause.getCause();
            if (currentCause != null) causeBuffer.append(" - ");
        }
        return causeBuffer.toString();
    }
}
