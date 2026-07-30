package com.bank.account.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionHoldResponseDTO {
    private String transactionId;
    private boolean success;
    private String message;
}
