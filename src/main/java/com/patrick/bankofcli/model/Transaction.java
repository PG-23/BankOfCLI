package com.patrick.bankofcli.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private int id;
    private TransactionType type;
    private BigDecimal amount;
    private Integer fromAccountId;
    private Integer toAccountId;
    private LocalDateTime timestamp;;

    public Transaction() {
    }

    public Transaction(int id, TransactionType type, BigDecimal amount,
                       Integer fromAccountId, Integer toAccountId, LocalDateTime timestamp) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Integer fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + type +
                ", amount=" + amount +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", timestamp=" + timestamp +
                "}";
    }
}
