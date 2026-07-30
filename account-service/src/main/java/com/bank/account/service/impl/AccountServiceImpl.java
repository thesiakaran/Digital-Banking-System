package com.bank.account.service.impl;

import com.bank.account.dto.request.CreateAccountRequestDTO;
import com.bank.account.dto.request.HoldFundsRequestDTO;
import com.bank.account.dto.response.AccountResponseDTO;
import com.bank.account.dto.response.TransactionHoldResponseDTO;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
        Account account = Account.builder()
                .accountnumber(generateAccountNumber())
                .accountholdername(request.getAccountholdername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .accounttype(request.getAcctype())
                .balance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccount(String accountnumber) {
        Account account = accountRepository.findByAccountnumber(accountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountnumber));
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public TransactionHoldResponseDTO holdFunds(HoldFundsRequestDTO request) {
        Account account = accountRepository.findByAccountnumber(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + request.getAccountNumber()));

        // Since heldBalance is removed, we directly deduct from balance for SAGA hold
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient available funds for account: " + request.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        log.info("Held {} for transaction {} on account {}", request.getAmount(), request.getTransactionId(), request.getAccountNumber());

        return TransactionHoldResponseDTO.builder()
                .transactionId(request.getTransactionId())
                .success(true)
                .message("Funds held successfully")
                .build();
    }

    @Override
    @Transactional
    public TransactionHoldResponseDTO releaseFunds(String accountnumber, BigDecimal amount, String transactionId) {
        Account account = accountRepository.findByAccountnumber(accountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountnumber));

        // Revert the deduction (rollback)
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        log.info("Released {} for transaction {} on account {}", amount, transactionId, accountnumber);

        return TransactionHoldResponseDTO.builder()
                .transactionId(transactionId)
                .success(true)
                .message("Funds released successfully")
                .build();
    }
    
    @Override
    @Transactional
    public TransactionHoldResponseDTO commitFunds(String accountnumber, BigDecimal amount, String transactionId) {
        Account account = accountRepository.findByAccountnumber(accountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountnumber));

        // Funds already deducted in holdFunds, so nothing needs to be changed on balance
        // We just log and return success
        log.info("Committed transfer of {} for transaction {} on account {}", amount, transactionId, accountnumber);

        return TransactionHoldResponseDTO.builder()
                .transactionId(transactionId)
                .success(true)
                .message("Funds committed successfully")
                .build();
    }

    @Override
    @Transactional
    public AccountResponseDTO deposit(String accountnumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountnumber(accountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountnumber));
        
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        
        log.info("Deposited {} into account {}", amount, accountnumber);
        
        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponseDTO withdraw(String accountnumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountnumber(accountnumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountnumber));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient available funds for account: " + accountnumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        
        log.info("Withdrew {} from account {}", amount, accountnumber);
        
        return mapToResponse(account);
    }

    private String generateAccountNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(java.util.concurrent.ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    private AccountResponseDTO mapToResponse(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId() != null ? account.getId().toString() : null)
                .accountnumber(account.getAccountnumber())
                .accountholdername(account.getAccountholdername())
                .email(account.getEmail())
                .phone(account.getPhone())
                .accounttype(account.getAccounttype())
                .status(account.getStatus())
                .balance(account.getBalance())
                .dailylimit(account.getDailylimit())
                .createdat(account.getCreatedat())
                .updatedat(account.getUpdatedat())
                .build();
    }
}
