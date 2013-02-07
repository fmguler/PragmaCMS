/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service.storage.domain;

/**
 * Represents the each storage object in the system.
 * Used to keep track of the key registry.
 * @author Fatih Mehmet Güler
 */
public class StorageObject {
    private String key;
    private Integer size;
    private String hash;

    public StorageObject(String key) {
        this.key = key;
    }

    public StorageObject(String key, Integer size, String hash) {
        this.key = key;
        this.size = size;
        this.hash = hash;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Storage Object, key: " + key + " size: " + size + " hash: " + hash;
    }
}
