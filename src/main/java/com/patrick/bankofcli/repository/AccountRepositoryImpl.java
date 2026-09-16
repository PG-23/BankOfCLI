package com.patrick.bankofcli.repository;

import com.patrick.bankofcli.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Account create(Connection conn, Account account) throws SQLException {
        String sql = "INSERT INTO accounts (pin, balance) VALUES (?, ?) RETURNING id";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
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
        String sql = "SELECT id, pin, balance FROM accounts WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
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
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setBigDecimal(1, newBalance);
            statement.setInt(2, id);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
