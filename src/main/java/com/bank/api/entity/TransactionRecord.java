package com.bank.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String transactionReference;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal runningBalance;

    private String remarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    public TransactionRecord() {}

    public TransactionRecord(String transactionReference, String accountNumber, BigDecimal amount, 
                             TransactionType type, BigDecimal runningBalance, String remarks) {
        this.transactionReference = transactionReference;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.runningBalance = runningBalance;
        this.remarks = remarks;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTransactionReference() { return transactionReference; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public BigDecimal getRunningBalance() { return runningBalance; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getTimestamp() { return timestamp; }
}