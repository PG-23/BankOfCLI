package com.patrick.bankofcli.api;

import com.patrick.bankofcli.business.AccountService;
import com.patrick.bankofcli.exception.AccountNotFoundException;
import com.patrick.bankofcli.exception.InsufficientFundsException;
import com.patrick.bankofcli.exception.InvalidPinException;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final AccountService accountService;
    private final Scanner scanner;
    private Account currentAccount;

    public ConsoleUI(AccountService accountService) {
        this.accountService = accountService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("=== Welcome to Bank of CLI ===");

        boolean running = true;
        while (running) {
            if (currentAccount == null) {
                running = showLoggedOutMenu();
            } else {
                showLoggedInMenu();
            }
        }

        System.out.println("Goodbye!");
        scanner.close();
    }

    // ****LOGGED-OUT MENU****

    private boolean showLoggedOutMenu() {
        System.out.println("\n1. Register\n2. Login\n3. Exit");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                handleRegister();
                return true;
            case "2":
                handleLogin();
                return true;
            case "3":
                return false;
            default:
                System.out.println("Invalid option, please try again.");
                return true;
        }
    }

    private void handleRegister() {
        System.out.print("Choose a 4-digit PIN: ");
        String pin = scanner.nextLine().trim();
        System.out.print("Enter initial deposit amount: ");
        BigDecimal amount = readAmount();
        if (amount == null) return;

        try {
            Account account = accountService.register(pin, amount);
            System.out.println("Account created! Your account ID is: " + account.getId());
        } catch (InvalidPinException | IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    private void handleLogin() {
        System.out.print("Enter account ID: ");
        Integer accountId = readAccountId();
        if (accountId == null) return;

        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine().trim();

        try {
            currentAccount = accountService.login(accountId, pin);
            System.out.println("Login successful. Welcome back!");
        } catch (AccountNotFoundException | InvalidPinException e) {
            System.out.println("Login failed: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    // ****LOGGED-IN MENU****

    private void showLoggedInMenu() {
        System.out.println("\n--- Account " + currentAccount.getId() + " ---");
        System.out.println("1. Check Balance\n2. Deposit\n3. Withdraw\n4. Transfer" +
                "\n5. Transaction History\n6. Logout");
        System.out.print("Choose an option: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": handleCheckBalance(); break;
            case "2": handleDeposit(); break;
            case "3": handleWithdraw(); break;
            case "4": handleTransfer(); break;
            case "5": handleTransactionHistory(); break;
            case "6":
                currentAccount = null;
                System.out.println("Logged out successfully.");
                break;
            default:
                System.out.println("Invalid option, please try again.");
        }
    }

    private void handleCheckBalance() {
        try {
            Account account = accountService.checkBalance(currentAccount.getId());
            System.out.println("Current balance: $" + account.getBalance());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    private void handleDeposit() {
        System.out.print("Enter deposit amount: ");
        BigDecimal amount = readAmount();
        if (amount == null) return;

        try {
            Account updated = accountService.deposit(currentAccount.getId(), amount);
            currentAccount = updated;
            System.out.println("Deposit successful. New balance: $" + updated.getBalance());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    private void handleWithdraw() {
        System.out.print("Enter withdrawal amount: ");
        BigDecimal amount = readAmount();
        if (amount == null) return;

        try {
            Account updated = accountService.withdraw(currentAccount.getId(), amount);
            currentAccount = updated;
            System.out.println("Withdrawal successful. New balance: $" + updated.getBalance());
        } catch (InsufficientFundsException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    private void handleTransfer() {
        System.out.print("Enter recipient account ID: ");
        Integer toAccountId = readAccountId();
        if (toAccountId == null) return;

        System.out.print("Enter transfer amount: ");
        BigDecimal amount = readAmount();
        if (amount == null) return;

        try {
            accountService.transfer(currentAccount.getId(), toAccountId, amount);
            currentAccount = accountService.checkBalance(currentAccount.getId());
            System.out.println("Transfer successful. New balance: $" + currentAccount.getBalance());
        } catch (InsufficientFundsException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    private void handleTransactionHistory() {
        try {
            List<Transaction> history = accountService.getTransactionHistory(currentAccount.getId());
            if (history.isEmpty()) {
                System.out.println("No transactions yet.");
                return;
            }
            System.out.println("\n--- Transaction History ---");
            for (Transaction t : history) {
                System.out.printf("[%s] %s - $%s (from: %s, to: %s)%n",
                        t.getTimestamp(), t.getType(), t.getAmount(),
                        t.getFromAccountId(), t.getToAccountId());
            }
        } catch (AccountNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Service unavailable. Please try again later.");
        }
    }

    // ****INPUT HANDLING****

    private BigDecimal readAmount() {
        String input = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(input);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Amount must be positive.");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
            return null;
        }
    }

    private Integer readAccountId() {
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid account ID entered.");
            return null;
        }
    }
}