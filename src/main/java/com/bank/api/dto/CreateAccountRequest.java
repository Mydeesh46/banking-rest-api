package com.bank.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank(message = "Account holder name cannot be blank")
    String accountHolderName,

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial deposit cannot be negative")
    BigDecimal initialDeposit
) {}