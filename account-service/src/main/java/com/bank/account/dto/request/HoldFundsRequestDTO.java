package com.bank.account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class HoldFundsRequestDTO {
    @NotBlank
    private String accountNumber;
    @Min(1)
    private BigDecimal amount;
    @NotBlank
    private String transactionId;
}
