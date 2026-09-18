package com.patrick.bankofcli;

import com.patrick.bankofcli.api.ConsoleUI;
import com.patrick.bankofcli.business.AccountService;
import com.patrick.bankofcli.repository.AccountRepositoryImpl;
import com.patrick.bankofcli.repository.TransactionRepositoryImpl;

public class Main {
    public static void main(String[] args) {
        AccountService accountService = new AccountService(
                new AccountRepositoryImpl(),
                new TransactionRepositoryImpl()
        );

        ConsoleUI consoleUI = new ConsoleUI(accountService);
        consoleUI.run();
    }
}