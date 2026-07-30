package com.bank.account.service;

import com.bank.account.dto.request.CreateAccountRequestDTO;
import com.bank.account.dto.request.HoldFundsRequestDTO;
import com.bank.account.dto.response.AccountResponseDTO;
import com.bank.account.dto.response.TransactionHoldResponseDTO;

import java.math.BigDecimal;

public interface AccountService {
    AccountResponseDTO createAccount(CreateAccountRequestDTO request);
    AccountResponseDTO getAccount(String accountNumber);
    TransactionHoldResponseDTO holdFunds(HoldFundsRequestDTO request);
    TransactionHoldResponseDTO releaseFunds(String accountNumber, BigDecimal amount, String transactionId);
    TransactionHoldResponseDTO commitFunds(String accountNumber, BigDecimal amount, String transactionId);
    AccountResponseDTO deposit(String accountNumber, BigDecimal amount);
    AccountResponseDTO withdraw(String accountNumber, BigDecimal amount);
}
