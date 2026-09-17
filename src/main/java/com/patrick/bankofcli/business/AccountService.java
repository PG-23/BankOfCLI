package com.patrick.bankofcli.business;

import com.patrick.bankofcli.config.ConnectionManager;
import com.patrick.bankofcli.exception.AccountNotFoundException;
import com.patrick.bankofcli.exception.InsufficientFundsException;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.model.Transaction;
import com.patrick.bankofcli.model.TransactionType;
import com.patrick.bankofcli.repository.AccountRepository;
import com.patrick.bankofcli.repository.TransactionRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ****WITHDRAW****

    public Account withdraw(int accountId, BigDecimal amount) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            Account result = withdraw(conn, accountId, amount);
            conn.commit();
            return result;
        }
    }

    // Package-private that contains actual business logic and is unit testable w/o DB connection
    Account withdraw(Connection conn, int accountId, BigDecimal amount) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with id " + accountId));
        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient funds: balance is "
                    + account.getBalance() + ", requested withdrawal is " + amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        accountRepository.updateBalance(conn, accountId, newBalance);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setFromAccountId(accountId);
        transaction.setToAccountId(null);
        transactionRepository.create(conn, transaction);

        return account;
    }

    // ****DEPOSIT****

    public Account deposit(int accountId, BigDecimal amount) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            Account result = deposit(conn, accountId, amount);
            conn.commit();
            return result;
        }
    }

    Account deposit (Connection conn, int accountId, BigDecimal amount) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with id " + accountId));

        BigDecimal newBalance = account.getBalance().add(amount);
        accountRepository.updateBalance(conn, accountId, newBalance);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setFromAccountId(null);
        transaction.setToAccountId(accountId);
        transactionRepository.create(conn, transaction);

        return account;
    }

    // ****TRANSFER****

    public void transfer(int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transfer(conn, fromAccountId, toAccountId, amount);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    void transfer(Connection conn, int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException {
        Account fromAccount = accountRepository.findById(conn, fromAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with id " + fromAccountId));

        Account toAccount = accountRepository.findById(conn, toAccountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with id " + toAccountId));

        if (amount.compareTo(fromAccount.getBalance()) > 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds: balance is " + fromAccount.getBalance()
                        + ", requested transfer is " + amount);
        }

        accountRepository.updateBalance(conn, fromAccountId, fromAccount.getBalance().subtract(amount));
        accountRepository.updateBalance(conn, toAccountId, toAccount.getBalance().add(amount));

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transactionRepository.create(conn, transaction);
    }
}
