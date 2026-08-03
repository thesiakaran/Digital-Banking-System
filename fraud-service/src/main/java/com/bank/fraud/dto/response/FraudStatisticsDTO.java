package com.bank.fraud.dto.response;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudStatisticsDTO {
    private long totalAlerts;
    private long openAlerts;
    private long investigatingAlerts;
    private long resolvedAlerts;
    private List<Map<String, Object>> topFlaggedAccounts;
}
