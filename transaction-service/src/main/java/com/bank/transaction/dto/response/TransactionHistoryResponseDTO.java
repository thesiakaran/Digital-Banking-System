package com.bank.transaction.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionHistoryResponseDTO {
    private String transactionId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER
    private BigDecimal amount;
    private String status;
    private String dateAndTime;
    private String relatedAccount; // Depending on type, it's either sender or receiver
}
