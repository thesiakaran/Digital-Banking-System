package com.bank.transaction.dto.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransactionEventDTO {
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private Long timestamp;
    private String correlationId;
}
