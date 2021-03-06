/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.account.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CMS User/Author, adds/edits content
 *
 * @author Fatih Mehmet Güler
 */
public class Author {
    private Integer id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Account account;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /*
     * ************************************************************
     * AUTHENTICATION RELATED METHODS
     * ************************************************************
     */
    /**
     * Check if the hashed password is equal to the passwordToCheck
     */
    public boolean checkPassword(String passwordToCheck) {
        try {
            byte[] hashedPw = MessageDigest.getInstance("MD5").digest(passwordToCheck.getBytes());
            passwordToCheck = toHex(hashedPw);
            return passwordToCheck.equals(password);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Author.class.getName()).log(Level.SEVERE, "Cannot hash user password", ex);
            return false;
        }
    }

    /**
     * @param passwordToReset set the new plain password
     */
    public void resetPassword(String passwordToReset) {
        try {
            byte[] hashedPw = MessageDigest.getInstance("MD5").digest(passwordToReset.getBytes());
            password = toHex(hashedPw);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Author.class.getName()).log(Level.SEVERE, "Cannot hash user password", ex);
        }
    }

    //byte array to hex
    protected static String toHex(byte[] bytes) {
        char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += hexChar[(bytes[i] & 0xf0) >> 4];
            result += hexChar[bytes[i] & 0x0f];
        }
        return result;
    }
}
