package com.patrick.bankofcli.repository;

import com.patrick.bankofcli.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction create(Connection conn, Transaction transaction) throws SQLException;

    Optional<Transaction> findById(Connection conn, int id) throws SQLException;

    List<Transaction> findByAccountId(Connection conn, int accountId) throws SQLException;
}
