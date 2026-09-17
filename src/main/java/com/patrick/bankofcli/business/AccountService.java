package com.patrick.bankofcli.business;

import com.patrick.bankofcli.config.ConnectionManager;
import com.patrick.bankofcli.exception.AccountNotFoundException;
import com.patrick.bankofcli.exception.InsufficientFundsException;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.repository.AccountRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account withdraw(int accountId, BigDecimal amount) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection()) {
            return withdraw(connection, accountId, amount);
        }
    }

    // Package-private that contains actual business logic and is unit testable w/o DB connection
    Account withdraw(Connection connection, int accountId, BigDecimal amount) throws SQLException {
        Account account = accountRepository.findById(connection, accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No account found with id " + accountId));
        if (amount.compareTo(account.getBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient funds: balance is "
                    + account.getBalance() + ", requested withdrawal is " + amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        accountRepository.updateBalance(connection, accountId, newBalance);
        account.setBalance(newBalance);

        return account;
    }
}
