package com.bank.api.controller;

import com.bank.api.dto.AccountResponse;
import com.bank.api.dto.CreateAccountRequest;
import com.bank.api.dto.DepositWithdrawRequest;
import com.bank.api.dto.TransferRequest;
import com.bank.api.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final BankService bankService;

    public AccountController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return new ResponseEntity<>(bankService.createAccount(request), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankService.getAccount(accountNumber));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(@Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(bankService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(bankService.withdraw(request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request) {
        bankService.transferFunds(request);
        return ResponseEntity.ok(Map.of("message", "Funds transferred successfully"));
    }
}