package com.bank.account.controller;

import com.bank.account.dto.request.CreateAccountRequestDTO;
import com.bank.account.dto.request.HoldFundsRequestDTO;
import com.bank.account.dto.response.AccountResponseDTO;
import com.bank.account.dto.response.TransactionHoldResponseDTO;
import com.bank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        return new ResponseEntity<>(accountService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @PostMapping("/hold")
    public ResponseEntity<TransactionHoldResponseDTO> holdFunds(@Valid @RequestBody HoldFundsRequestDTO request) {
        return ResponseEntity.ok(accountService.holdFunds(request));
    }

    @PostMapping("/{accountNumber}/release")
    public ResponseEntity<TransactionHoldResponseDTO> releaseFunds(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(accountService.releaseFunds(accountNumber, amount, transactionId));
    }
    
    @PostMapping("/{accountNumber}/commit")
    public ResponseEntity<TransactionHoldResponseDTO> commitFunds(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(accountService.commitFunds(accountNumber, amount, transactionId));
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponseDTO> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.deposit(accountNumber, amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponseDTO> withdraw(
            @Valid @RequestBody com.bank.account.dto.request.WithdrawRequestDTO request) {
        return ResponseEntity.ok(accountService.withdraw(request.getAccountNumber(), request.getAmount()));
    }
}
