package com.bank.api.service;

import com.bank.api.dto.TransactionResponse;
import com.bank.api.exception.ResourceNotFoundException;
import com.bank.api.repository.AccountRepository;
import com.bank.api.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        if (!accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new ResourceNotFoundException("Account not found: " + accountNumber);
        }

        return transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber).stream()
            .map(tx -> new TransactionResponse(
                tx.getTransactionReference(),
                tx.getAccountNumber(),
                tx.getAmount(),
                tx.getType(),
                tx.getRunningBalance(),
                tx.getRemarks(),
                tx.getTimestamp()
            ))
            .toList();
    }
}