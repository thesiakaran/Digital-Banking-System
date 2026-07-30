package com.bank.account.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDTO {
    
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than zero")
    private BigDecimal amount;
}
