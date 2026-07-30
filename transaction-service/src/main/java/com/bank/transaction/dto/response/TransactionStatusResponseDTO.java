package com.bank.transaction.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionStatusResponseDTO {
    private BigDecimal amount;
    private String status;
    private String dateAndTime;
}
