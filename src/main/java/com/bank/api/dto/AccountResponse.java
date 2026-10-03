package com.bank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
    String accountNumber,
    String accountHolderName,
    BigDecimal balance,
    LocalDateTime createdAt
) {}