package com.patrick.bankofcli.repository;

import com.patrick.bankofcli.model.Transaction;
import com.patrick.bankofcli.model.TransactionType;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public Transaction create(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (type, amount, from_account_id, to_account_id) " +
                "VALUES (?::transaction_type, ?, ?, ?) RETURNING id, timestamp";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, transaction.getType().name().toLowerCase());
            statement.setBigDecimal(2, transaction.getAmount());
            setNullableInt(statement, 3, transaction.getFromAccountId());
            setNullableInt(statement, 4, transaction.getToAccountId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    transaction.setId(resultSet.getInt("id"));
                    transaction.setTimestamp(resultSet.getTimestamp("timestamp").toLocalDateTime());
                }
            }
        }
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, type, amount, from_account_id, to_account_id, timestamp " +
                "FROM transactions WHERE id = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findByAccountId(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT id, type, amount, from_account_id, to_account_id, timestamp " +
                "FROM transactions " +
                "WHERE from_account_id = ? OR to_account_id = ? " +
                "ORDER BY timestamp DESC";

        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setInt(2, accountId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    transactions.add(mapRow(resultSet));
                }
            }
        }
        return transactions;
    }

    private Transaction mapRow(ResultSet resultSet) throws SQLException {
        return new Transaction(
                resultSet.getInt("id"),
                TransactionType.valueOf(resultSet.getString("type").toUpperCase()),
                resultSet.getBigDecimal("amount"),
                (Integer) resultSet.getObject("from_account_id"),
                (Integer) resultSet.getObject("to_account_id"),
                resultSet.getTimestamp("timestamp").toLocalDateTime()
        );
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }
}
