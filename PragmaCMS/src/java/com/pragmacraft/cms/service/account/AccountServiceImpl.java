/*
 *  PragmaCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.pragmacraft.cms.service.account;

import com.fmguler.ven.Criteria;
import com.fmguler.ven.Ven;
import com.pragmacraft.cms.service.account.domain.Account;
import com.pragmacraft.cms.service.account.domain.Author;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Handles account operations
 *
 * @author Fatih Mehmet Güler
 */
public class AccountServiceImpl implements AccountService {
    private Ven ven;

    //--------------------------------------------------------------------------
    //AUTHOR
    //--------------------------------------------------------------------------
    @Override
    public Author getAuthor(String username) {
        Set joins = new HashSet();
        joins.add("Author.account.sites");
        Criteria criteria = new Criteria();
        criteria.eq("Author.username", username);
        List<Author> list = ven.list(Author.class, joins, criteria);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    @Override
    public Author getAuthor(int authorId) {
        Set joins = new HashSet();
        joins.add("Author.account");
        Criteria criteria = new Criteria();
        return (Author)ven.get(authorId, Author.class, joins);
    }

    @Override
    public void saveAuthor(Author author) {
        ven.save(author);
    }

    @Override
    public void removeAuthor(int id) {
        ven.delete(id, Author.class);
    }

    //--------------------------------------------------------------------------
    //ACCOUNT
    //--------------------------------------------------------------------------
    @Override
    public Account getAccount(int accountId) {
        Set joins = new HashSet();
        joins.add("Account.sites");
        joins.add("Account.authors");
        joins.add("Account.primaryContact");
        Criteria criteria = new Criteria();
        criteria.orderAsc("Account.sites.id");
        criteria.orderAsc("Account.authors.id");

        return (Account)ven.get(accountId, Account.class, joins,criteria);
    }

    @Override
    public void saveAccount(Account account) {
        ven.save(account);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    //--------------------------------------------------------------------------
    public void setDataSource(DataSource dataSource) {
        ven = new Ven();
        ven.setDataSource(dataSource);
        ven.addDomainPackage("com.pragmacraft.cms.service.account.domain");
        ven.addDomainPackage("com.pragmacraft.cms.service.content.domain");
    }
}
