package com.bank.transaction.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InitiateTransactionDTO {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    @Min(1)
    private BigDecimal amount;
    private com.bank.transaction.entity.TransactionType type;
}
