package com.bank.fraud.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlertResponseDTO {
    private Long id;
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private String reason;
    private int riskScore;
    private String status;
    private String severity;
    private String detectedAt;
    private String resolvedAt;
}
