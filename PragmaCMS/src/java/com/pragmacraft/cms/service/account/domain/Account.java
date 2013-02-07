/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.account.domain;

import com.pragmacraft.cms.service.content.domain.Site;
import com.fmguler.ven.util.VenList;
import java.util.List;

/**
 * Holds the info about each account which can have multiple web sites and
 * authors (an account can have multiple authors)
 *
 * @author Fatih Mehmet Güler
 */
public class Account {
    private Integer id;
    private String company;
    private String address;
    private String city;
    private String state;
    private String country;
    private String phone;
    private Author primaryContact;
    private List<Author> authors = new VenList(Author.class, "account"); //authors of this account
    private List<Site> sites = new VenList(Site.class, "account"); //sites of this account

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
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the primaryContact
     */
    public Author getPrimaryContact() {
        return primaryContact;
    }

    /**
     * @param primaryContact the primaryContact to set
     */
    public void setPrimaryContact(Author primaryContact) {
        this.primaryContact = primaryContact;
    }

    /**
     * @return the authors
     */
    public List getAuthors() {
        return authors;
    }

    /**
     * @param authors the authors to set
     */
    public void setAuthors(List authors) {
        this.authors = authors;
    }

    /**
     * @return the sites
     */
    public List getSites() {
        return sites;
    }

    /**
     * @param sites the sites to set
     */
    public void setSites(List sites) {
        this.sites = sites;
    }

    /**
     * Check if this account contains this site
     */
    public boolean checkSite(Integer siteId) {
        for (Site site : sites) {
            if (site.getId().equals(siteId)) return true;
        }
        return false;
    }
}
