package com.bank.fraud.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudResultEventDTO {
    private String transactionId;
    private Boolean approved;
    private String reason;
    private Long evaluatedAt;
}
