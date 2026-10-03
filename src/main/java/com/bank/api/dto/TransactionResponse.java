package com.bank.api.dto;

import com.bank.api.entity.TransactionRecord.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    String transactionReference,
    String accountNumber,
    BigDecimal amount,
    TransactionType type,
    BigDecimal runningBalance,
    String remarks,
    LocalDateTime timestamp
) {}