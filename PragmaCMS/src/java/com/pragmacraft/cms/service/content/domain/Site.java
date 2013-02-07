/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.content.domain;

import com.pragmacraft.cms.service.account.domain.Account;

/**
 * Holds the info about each web site hosted by PragmaCMS
 * Differentiates between distinct sites by domain names.
 *
 * @author Fatih Mehmet Güler
 */
public class Site {
    private Integer id;
    private String domains; //white space separated domain names of this site
    private Account account;

    public Site() {
    }

    public Site(Integer id) {
        this.id = id;
    }

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
     * @return the domains
     */
    public String getDomains() {
        return domains;
    }

    /**
     * @param domains the domains to set
     */
    public void setDomains(String domains) {
        this.domains = domains;
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

    /**
     * @return the domain name array (parsed from domains)
     */
    public String[] toDomainArray(){
        if (domains==null) return new String[0];
        return domains.split("\\s+"); //white space separated domain name regexes
    }

}
