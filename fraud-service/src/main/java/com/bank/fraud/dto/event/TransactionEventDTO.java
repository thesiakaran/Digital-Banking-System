package com.bank.fraud.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventDTO {
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private Long timestamp;
    private String correlationId;
}
