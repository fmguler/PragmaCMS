/*
 *  Common Services
 *  Copyright 2012 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.pragmacraft.common.service.storage.dao;

import com.pragmacraft.common.service.storage.domain.StorageObject;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 *
 * @author Fatih Mehmet Güler
 */
public class StorageDaoPostgresImpl implements StorageDao {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private String tablePrefix;

    public void insertStorageObject(StorageObject object) {
        String newSql = "INSERT INTO " + tablePrefix + "STORAGE_OBJECT(key, size, hash) VALUES (:key, :size, :hash);";
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(object);
        jdbcTemplate.update(newSql, parameterSource);
    }

    public void updateStorageObject(StorageObject object) {
        String updateSql = "UPDATE " + tablePrefix + "STORAGE_OBJECT SET size=:size, hash=:hash WHERE key = :key";
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(object);
        jdbcTemplate.update(updateSql, parameterSource);
    }

    public StorageObject getStorageObject(String key) {
        String sql = "SELECT * from " + tablePrefix + "STORAGE_OBJECT where key = :key;";
        Map paramMap = new HashMap();
        paramMap.put("key", key);
        List elems = jdbcTemplate.queryForList(sql, paramMap);
        if (elems.isEmpty()) return null;

        return mapStorageObject((Map)elems.get(0), "");
    }

    public StorageObject getStorageObjectByHash(String hash) {
        String sql = "SELECT * from " + tablePrefix + "STORAGE_OBJECT where hash = :hash;";
        Map paramMap = new HashMap();
        paramMap.put("hash", hash);
        List elems = jdbcTemplate.queryForList(sql, paramMap);
        if (elems.isEmpty()) return null;

        return mapStorageObject((Map)elems.get(0), "");
    }

    public void deleteStorageObject(String key) {
        String sql = "DELETE FROM " + tablePrefix + "STORAGE_OBJECT where key = :key";
        Map paramMap = new HashMap();
        paramMap.put("key", key);
        jdbcTemplate.update(sql, paramMap);
    }

    public List findOrphanObjects(List tablesToJoin) {
        StringBuffer joinTables = new StringBuffer();
        Iterator it = tablesToJoin.iterator();
        while (it.hasNext()) {
            Map table = (Map)it.next();
            joinTables.append("select ").append(table.get("fieldName")).append(" as key from ").append(tablePrefix).append(table.get("tableName")).append(" union ");
        }
        joinTables.append("select null");

        String sql1 = "select s.key, s.size, s.hash from " + tablePrefix + "STORAGE_OBJECT s "
                + "left join "
                + "("
                + joinTables.toString()
                + ") all_keys on all_keys.key = s.key "
                + "where all_keys.key is null ";

        Map paramMap = new HashMap();
        List elems = jdbcTemplate.queryForList(sql1, paramMap); //first get objectss to be deleted

        List orphanObjects = new LinkedList();
        it = elems.iterator();
        while (it.hasNext()) {
            Map elem = (Map)it.next();
            orphanObjects.add(mapStorageObject(elem, ""));
        }
        return orphanObjects;
    }

    //-------MAPPERS------------------------------------------------------------
    private StorageObject mapStorageObject(Map elem, String prefix) {
        StorageObject obj = new StorageObject((String)elem.get(prefix + "key"));
        if (obj.getKey() == null) return obj;
        obj.setSize((Integer)elem.get(prefix + "size"));
        obj.setHash((String)elem.get(prefix + "hash"));
        return obj;
    }

    //--------------------------------------------------------------------------
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void setDatabaseConfiguration(Map databaseConfiguration) {
        tablePrefix = (String)databaseConfiguration.get("tablePrefix");
        if (tablePrefix == null) tablePrefix = "";
    }
}
