package com.patrick.bankofcli.repository;

import com.patrick.bankofcli.model.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.math.BigDecimal;

public interface AccountRepository {

    Account create(Connection conn, Account account) throws SQLException;

    Optional<Account> findById(Connection conn, int id) throws SQLException;

    boolean updateBalance(Connection conn, int id, BigDecimal newBalance) throws SQLException;
}
