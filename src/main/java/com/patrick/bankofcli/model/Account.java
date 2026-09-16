package com.patrick.bankofcli.model;

import java.math.BigDecimal;

public class Account {

    private int id;
    private String pin;
    private BigDecimal balance;

    public Account () {
    }

    public Account (int id, String pin, BigDecimal balance) {
        this.id = id;
        this.pin = pin;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{id=" + id
                + ", pin=" + pin
                + ", balance=" + balance +
                "}";
    }
}
