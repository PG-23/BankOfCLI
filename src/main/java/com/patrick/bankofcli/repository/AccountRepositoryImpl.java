package com.patrick.bankofcli.repository;

import com.patrick.bankofcli.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    private static final String SQL_CREATE =
            "INSERT INTO accounts (pin, balance) VALUES (?, ?) RETURNING id";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, pin, balance FROM accounts WHERE id = ?";

    private static final String SQL_UPDATE_BALANCE =
            "UPDATE accounts SET balance = ? WHERE id = ?";

    @Override
    public Account create(Connection conn, Account account) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(SQL_CREATE)) {
            statement.setString(1, account.getPin());
            statement.setBigDecimal(2, account.getBalance());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    account.setId(resultSet.getInt("id"));
                }
            }
        }
        return account;
    }

    @Override
    public Optional<Account> findById(Connection conn, int id) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(SQL_FIND_BY_ID)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Account account = new Account (
                            resultSet.getInt("id"),
                            resultSet.getString("pin"),
                            resultSet.getBigDecimal("balance")
                    );
                    return Optional.of(account);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateBalance(Connection conn, int id, BigDecimal newBalance) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_BALANCE)) {
            statement.setBigDecimal(1, newBalance);
            statement.setInt(2, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
