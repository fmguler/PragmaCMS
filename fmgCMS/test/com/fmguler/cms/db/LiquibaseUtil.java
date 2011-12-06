/*
 *  fmgCMS
 *  Copyright 2011 PragmaCraft LLC.
 * 
 *  All rights reserved.
 */
package com.fmguler.cms.db;

import java.sql.SQLException;
import java.util.Locale;
import javax.sql.DataSource;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Database utilities for testing
 * 
 * @author Fatih Mehmet Güler
 */
public class LiquibaseUtil {
    /**
     * @return DataSource for the test database
     */
    public static DataSource getDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUsername("postgres");
        ds.setPassword("qwerty");
        ds.setUrl("jdbc:postgresql://127.0.0.1:5432/fmgcmsdb");
        return ds;
    }

    /**
     * Build the test database
     */
    public static void buildDatabase() {
        try {
            Locale currLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(getDataSource().getConnection());
            Liquibase liquibase = new Liquibase("web/WEB-INF/database-changelog.xml", new FileSystemFileOpener(), database);
            liquibase.update("");
            Locale.setDefault(currLocale);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (JDBCException ex) {
            ex.printStackTrace();
        } catch (LiquibaseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Undo all changes in the test database
     */
    public static void rollbackDatabase(String tag, int count) {
        try {
            Locale currLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(getDataSource().getConnection());
            Liquibase liquibase = new Liquibase("web/WEB-INF/database-changelog.xml", new FileSystemFileOpener(), database);
            if (tag == null) liquibase.rollback(count, "");
            else liquibase.rollback(tag, "");
            Locale.setDefault(currLocale);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (JDBCException ex) {
            ex.printStackTrace();
        } catch (LiquibaseException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Quick test
     */
    public static void main(String[] args) {
        buildDatabase();
        //rollbackDatabase("tag-init", 0);
        //rollbackDatabase(null, 2);

    }
}
