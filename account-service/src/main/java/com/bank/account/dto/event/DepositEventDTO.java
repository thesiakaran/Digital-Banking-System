package com.bank.account.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositEventDTO {
    private String transactionId;
    private String receiverAccountId;
    private BigDecimal amount;
    private Long timestamp;
}
