/*
 *  fmgCMS
 *  Copyright 2012 PragmaCraft LLC.
 *
 *  All rights reserved.
 */
package com.fmguler.cms.service.account;

import com.fmguler.cms.service.account.domain.Account;
import com.fmguler.cms.service.account.domain.Author;

/**
 * Handles account operations
 *
 * @author Fatih Mehmet Güler
 */
public interface AccountService {
    /**
     * Get the author by username
     * @param username unique username
     * @return
     */
    Author getAuthor(String username);

    /**
     * Get the account by id
     * @param accountId account id
     * @return
     */
    Account getAccount(int accountId);

    Author getAuthor(int authorId);

    void removeAuthor(int id);

    void saveAuthor(Author author);

    void saveAccount(Account account);
}
