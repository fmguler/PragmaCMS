/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.account;

import com.fmguler.cms.service.account.domain.Account;
import com.fmguler.cms.service.account.domain.Author;
import com.fmguler.ven.Criteria;
import com.fmguler.ven.Ven;
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

        return (Account)ven.get(accountId, Account.class, joins,criteria);
    }

    //--------------------------------------------------------------------------
    //SETTERS
    //--------------------------------------------------------------------------
    public void setDataSource(DataSource dataSource) {
        ven = new Ven();
        ven.setDataSource(dataSource);
        ven.addDomainPackage("com.fmguler.cms.service.account.domain");
        ven.addDomainPackage("com.fmguler.cms.service.content.domain");
    }
}
