package com.patrick.bankofcli;

import com.patrick.bankofcli.business.AccountService;
import com.patrick.bankofcli.config.ConnectionManager;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.repository.AccountRepositoryImpl;
import com.patrick.bankofcli.repository.TransactionRepositoryImpl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        AccountService accountService = new AccountService(
                new AccountRepositoryImpl(),
                new TransactionRepositoryImpl()
        );

        // Test for log INFO: successful registration
        Account newAccount = accountService.register("1234", new BigDecimal("500.00"));
        System.out.println("Registered: " + newAccount);

        // Test for log INFO: successful login
        accountService.login(newAccount.getId(), "1234");

        // Test for log ERROR: incorrect PIN
        try {
            accountService.login(newAccount.getId(), "9999");
        } catch (Exception e) {
            System.out.println("Expected failure: " + e.getMessage());
        }

        // Test for log ERROR: insufficient funds
        try {
            accountService.withdraw(newAccount.getId(), new BigDecimal("999999.00"));
        } catch (Exception e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}
