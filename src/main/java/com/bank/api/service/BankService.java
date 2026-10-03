package com.bank.api.service;

import com.bank.api.dto.*;
import com.bank.api.entity.Account;
import com.bank.api.entity.TransactionRecord;
import com.bank.api.entity.TransactionRecord.TransactionType;
import com.bank.api.exception.InsufficientBalanceException;
import com.bank.api.exception.ResourceNotFoundException;
import com.bank.api.repository.AccountRepository;
import com.bank.api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class BankService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest req) {
        String accountNumber = "ACC" + (10000000 + new SecureRandom().nextInt(90000000));
        Account account = new Account(accountNumber, req.accountHolderName(), req.initialDeposit());
        Account savedAccount = accountRepository.save(account);

        if (req.initialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            TransactionRecord tx = new TransactionRecord(
                UUID.randomUUID().toString(),
                savedAccount.getAccountNumber(),
                req.initialDeposit(),
                TransactionType.DEPOSIT,
                savedAccount.getBalance(),
                "Initial Account Opening Deposit"
            );
            transactionRepository.save(tx);
        }

        return mapToAccountResponse(savedAccount);
    }

    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return mapToAccountResponse(account);
    }

    @Transactional
    public AccountResponse deposit(DepositWithdrawRequest req) {
        Account account = accountRepository.findByAccountNumber(req.accountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + req.accountNumber()));

        account.setBalance(account.getBalance().add(req.amount()));
        Account savedAccount = accountRepository.save(account);

        TransactionRecord tx = new TransactionRecord(
            UUID.randomUUID().toString(),
            account.getAccountNumber(),
            req.amount(),
            TransactionType.DEPOSIT,
            savedAccount.getBalance(),
            "Deposit operation"
        );
        transactionRepository.save(tx);

        return mapToAccountResponse(savedAccount);
    }

    @Transactional
    public AccountResponse withdraw(DepositWithdrawRequest req) {
        Account account = accountRepository.findByAccountNumber(req.accountNumber())
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + req.accountNumber()));

        if (account.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds. Available: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(req.amount()));
        Account savedAccount = accountRepository.save(account);

        TransactionRecord tx = new TransactionRecord(
            UUID.randomUUID().toString(),
            account.getAccountNumber(),
            req.amount(),
            TransactionType.WITHDRAWAL,
            savedAccount.getBalance(),
            "Withdrawal operation"
        );
        transactionRepository.save(tx);

        return mapToAccountResponse(savedAccount);
    }

    @Transactional
    public void transferFunds(TransferRequest req) {
        if (req.fromAccount().equalsIgnoreCase(req.toAccount())) {
            throw new IllegalArgumentException("Source and destination accounts cannot be identical");
        }

        Account source = accountRepository.findByAccountNumber(req.fromAccount())
            .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + req.fromAccount()));

        Account target = accountRepository.findByAccountNumber(req.toAccount())
            .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + req.toAccount()));

        if (source.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for transfer. Available: " + source.getBalance());
        }

        source.setBalance(source.getBalance().subtract(req.amount()));
        target.setBalance(target.getBalance().add(req.amount()));

        accountRepository.save(source);
        accountRepository.save(target);

        String transferRef = UUID.randomUUID().toString();

        TransactionRecord debitTx = new TransactionRecord(
            transferRef + "-DEBIT",
            source.getAccountNumber(),
            req.amount(),
            TransactionType.TRANSFER_OUT,
            source.getBalance(),
            "Transferred to " + target.getAccountNumber() + (req.remarks() != null ? " - " + req.remarks() : "")
        );

        TransactionRecord creditTx = new TransactionRecord(
            transferRef + "-CREDIT",
            target.getAccountNumber(),
            req.amount(),
            TransactionType.TRANSFER_IN,
            target.getBalance(),
            "Received from " + source.getAccountNumber() + (req.remarks() != null ? " - " + req.remarks() : "")
        );

        transactionRepository.save(debitTx);
        transactionRepository.save(creditTx);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
            account.getAccountNumber(),
            account.getAccountHolderName(),
            account.getBalance(),
            account.getCreatedAt()
        );
    }
}