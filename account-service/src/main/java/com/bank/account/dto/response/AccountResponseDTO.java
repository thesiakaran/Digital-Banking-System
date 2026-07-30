package com.bank.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponseDTO {
    private String id;

    private String accountnumber;

    private String accountholdername;

    private String email;

    private String phone;

    private AccountType accounttype;

    private AccountStatus status;

    private BigDecimal balance;

    private BigDecimal dailylimit;

    private LocalDateTime createdat;

    private LocalDateTime updatedat;
}
