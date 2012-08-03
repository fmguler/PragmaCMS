/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.content.domain;

import com.fmguler.ven.util.VenList;
import java.util.List;

/**
 * Holds the info about each web site hosted by fmgCMS
 * Differentiates between distinct sites by domain names.
 *
 * @author Fatih Mehmet Güler
 */
public class Site {
    private Integer id;
    private String domains; //white space separated domain names of this site
    private String company;
    private String address;
    private String city;
    private String state;
    private String country;
    private String phone;
    private Author primaryContact;
    private List authors = new VenList(Author.class, "site"); //authors of this site

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
}
