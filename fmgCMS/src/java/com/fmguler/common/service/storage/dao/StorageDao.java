/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.common.service.storage.dao;

import com.fmguler.common.service.storage.domain.StorageObject;
import java.util.List;

/**
 *
 * @author Fatih Mehmet Güler
 */
public interface StorageDao {
    /**
     * Inserts new storage object
     * @param object the storage object having key
     */
    void insertStorageObject(StorageObject object);

    /**
     * Updates an existing storage object, e.g. to set hash/size
     * @param object the storage object having key
     */
    void updateStorageObject(StorageObject object);

    /**
     * Using the tablesToJoin, calculates orphan storage objects.
     * <p>
     * Orphan storage objects are the ones which are not referenced from the
     * specified table fields.
     * <p>
     * Caller can use the result to delete the orphans or take any action on
     * them.
     * @param tablesToJoin the list of maps, each map includes tableName and
     * fieldName keys in which the storage keys are referenced.
     * @return the list of StroageObject which are orphan (not referenced from
     * any of the specified tables/fields)
     */
    List findOrphanObjects(List tablesToJoin);

    /**
     * Deletes the storage object with the specified key
     * @param key the storage key
     */
    void deleteStorageObject(String key);

    /**
     * Returns the storage object with the specified key
     * @param key the storage key
     * @return the StorageObject
     */
    StorageObject getStorageObject(String key);

    /**
     * Returns the storage object with the specified hash value
     * @param hash the SHA1 hash value of the storage content
     * @return the StorageObject
     */
    StorageObject getStorageObjectByHash(String hash);
}
