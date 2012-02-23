/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service.storage;

/**
 *
 * @author Fatih Mehmet Güler
 */
public interface StorageOutputStream {
    /**
     * @return the storage key for this content
     */
    public String getKey();

    /**
     * @return the size of the total written bytes
     */
    public int getSize();

    /**
     * @return the hash of the written content
     */
    public String getHash();
}
