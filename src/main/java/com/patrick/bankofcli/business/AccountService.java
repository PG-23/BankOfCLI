package com.patrick.bankofcli.business;

import com.patrick.bankofcli.config.ConnectionManager;
import com.patrick.bankofcli.exception.AccountNotFoundException;
import com.patrick.bankofcli.exception.InsufficientFundsException;
import com.patrick.bankofcli.exception.InvalidPinException;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.model.Transaction;
import com.patrick.bankofcli.model.TransactionType;
import com.patrick.bankofcli.repository.AccountRepository;
import com.patrick.bankofcli.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // ****REGISTER****

    public Account register(String pin, BigDecimal initialBalance) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return register(conn, pin, initialBalance);

        } catch (SQLException e) {
            logger.error("Database error during registration: {}", e.getMessage());
            throw e;
        }
    }

    Account register(Connection conn, String pin, BigDecimal initialBalance) throws SQLException {
        if (pin == null || !pin.matches("\\d{4}")) {
            logger.error("Registration failed: invalid PIN format provided");
            throw new InvalidPinException("PIN must be exactly 4 digits");
        }
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.error("Registration failed: negative initial balance ({})", initialBalance);
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Account account = new Account();
        account.setPin(pin);
        account.setBalance(initialBalance);

        Account created = accountRepository.create(conn, account);
        logger.info("New account registered successfully with id {}", created.getId());
        return created;
    }

    // ****LOGIN****

    public Account login(int accountId, String pin) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return login(conn, accountId, pin);
        } catch (SQLException e) {
            logger.error("Database error during login for account {}: {}", accountId, e.getMessage());
            throw e;
        }
    }

    Account login(Connection conn, int accountId, String pin) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> {
                    logger.error("Login failed: no account found with id {}", accountId);
                    return new AccountNotFoundException("No account found with id " + accountId);
                });
        // Future improvement would include evaluating a hashed pin rather than string
        // evaluation as implemented below
        if (!account.getPin().equals(pin)) {
            logger.error("Login failed: incorrect PIN entered for account {}", accountId);
            throw new InvalidPinException("Incorrect PIN for account " + accountId);
        }

        logger.info("User successfully logged into account {}", accountId);
        return account;
    }

    // ****CHECK BALANCE****

    public Account checkBalance(int accountId) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return checkBalance(conn, accountId);
        } catch (SQLException e) {
            logger.error("Database error while checking for balance for account {}: {}", accountId, e.getMessage());
            throw e;
        }
    }

    Account checkBalance(Connection conn, int accountId) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> {
                    logger.error("Balance check failed: no account found with id {}", accountId);
                    return new AccountNotFoundException("No account found with id " + accountId);
                });

        logger.info("Balance checked successfully for account {}", accountId);
        return account;
    }

    // ****WITHDRAW****

    public Account withdraw(int accountId, BigDecimal amount) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return withdraw(conn, accountId, amount);
        } catch (SQLException e) {
            logger.error("Database error during withdrawal for account {}: {}", accountId, e.getMessage());
            throw e;
        }
    }

    // Package-private that contains actual business logic and is unit testable w/o DB connection
    Account withdraw(Connection conn, int accountId, BigDecimal amount) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> {
                    logger.error("Withdrawal failed: no account found with id {}", accountId);
                    return new AccountNotFoundException("No account found with id " + accountId);
                });

        if (amount.compareTo(account.getBalance()) > 0) {
            logger.error("Withdrawal failed for account {}: insufficient funds (balance {}, requested {})",
                    accountId, account.getBalance(), amount);
            throw new InsufficientFundsException("Insufficient funds: balance is " + account.getBalance()
                    + ", requested withdrawal is " + amount);
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

        logger.info("Withdrawal of {} successful for account {}. New balance: {}", amount, accountId, newBalance);
        return account;
    }

    // ****DEPOSIT****

    public Account deposit(int accountId, BigDecimal amount) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return deposit(conn, accountId, amount);
        } catch (SQLException e) {
            logger.error("Database error during deposit for account {}: {}", accountId, e.getMessage());
            throw e;
        }
    }

    Account deposit (Connection conn, int accountId, BigDecimal amount) throws SQLException {
        Account account = accountRepository.findById(conn, accountId)
                .orElseThrow(() -> {
                    logger.error("Deposit failed: no account found with id {}", accountId);
                    return new AccountNotFoundException("No account found with id " + accountId);
                });

        BigDecimal newBalance = account.getBalance().add(amount);
        accountRepository.updateBalance(conn, accountId, newBalance);
        account.setBalance(newBalance);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setFromAccountId(null);
        transaction.setToAccountId(accountId);
        transactionRepository.create(conn, transaction);

        logger.info("Deposit of {} successful for account {}. New balance: {}", amount, accountId, newBalance);
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
                logger.error("Transfer rolled back from account {} to account {}: {}", fromAccountId, toAccountId,
                        e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            logger.error("Database error during transfer from account {} to account {}: {}",
                    fromAccountId, toAccountId, e.getMessage());
            throw e;
        }
    }

    // Entire transfer method below is atomic in nature due to the wrapper transfer method ensuring
    // conn.setAutoCommit -> false before this package-private method is called
    void transfer(Connection conn, int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException {
        Account fromAccount = accountRepository.findById(conn, fromAccountId)
                .orElseThrow(() -> {
                    logger.error("Transfer failed: no account found with id {}", fromAccountId);
                    return new AccountNotFoundException("No account found with id " + fromAccountId);
                });

        Account toAccount = accountRepository.findById(conn, toAccountId)
                .orElseThrow(() -> {
                    logger.error("Transfer failed: no account found with id {}", toAccountId);
                    return new AccountNotFoundException("No account found with id " + toAccountId);
                });

        if (amount.compareTo(fromAccount.getBalance()) > 0) {
            logger.error("Transfer failed from account {}: insufficient funds (balance {}, requested {})",
                    fromAccountId, fromAccount.getBalance(), amount);
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

        logger.info("Transfer of {} successful from account {} to account {}", amount, fromAccount, toAccount);
    }

    // ****TRANSACTION HISTORY****

    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            return getTransactionHistory(conn, accountId);
        } catch (SQLException e) {
            logger.error("Database error retrieving transaction history for account {}: {}", accountId, e.getMessage());
            throw e;
        }
    }

    List<Transaction> getTransactionHistory(Connection conn, int accountId) throws SQLException {
        // Verifies the account exists before returning its history
        accountRepository.findById(conn, accountId)
                .orElseThrow(() -> {
                    logger.error("Transaction history request failed: no account found with id {}", accountId);
                    return new AccountNotFoundException("No account found with id " + accountId);
                });

        List<Transaction> history = transactionRepository.findByAccountId(conn, accountId);
        logger.info("Transaction history retrieved for account {} ({} records)", accountId, history.size());
        return history;
    }
}