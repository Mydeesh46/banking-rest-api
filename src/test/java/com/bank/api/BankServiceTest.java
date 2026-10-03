package com.bank.api;

import com.bank.api.dto.CreateAccountRequest;
import com.bank.api.dto.AccountResponse;
import com.bank.api.dto.TransferRequest;
import com.bank.api.entity.Account;
import com.bank.api.exception.InsufficientBalanceException;
import com.bank.api.repository.AccountRepository;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BankService bankService;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account("ACC10001", "Alice", new BigDecimal("1000.00"));
        targetAccount = new Account("ACC10002", "Bob", new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Create account successfully with initial balance")
    void testCreateAccountSuccess() {
        CreateAccountRequest req = new CreateAccountRequest("Alice", new BigDecimal("1000.00"));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount);

        AccountResponse res = bankService.createAccount(req);

        assertNotNull(res);
        assertEquals("Alice", res.accountHolderName());
        assertEquals(new BigDecimal("1000.00"), res.balance());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Transfer funds successfully between accounts")
    void testTransferFundsSuccess() {
        TransferRequest req = new TransferRequest("ACC10001", "ACC10002", new BigDecimal("300.00"), "Rent");
        when(accountRepository.findByAccountNumber("ACC10001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC10002")).thenReturn(Optional.of(targetAccount));

        bankService.transferFunds(req);

        assertEquals(new BigDecimal("700.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("800.00"), targetAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Throw InsufficientBalanceException when source balance is inadequate")
    void testTransferInsufficientFunds() {
        TransferRequest req = new TransferRequest("ACC10001", "ACC10002", new BigDecimal("1500.00"), "Transfer");
        when(accountRepository.findByAccountNumber("ACC10001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC10002")).thenReturn(Optional.of(targetAccount));

        assertThrows(InsufficientBalanceException.class, () -> bankService.transferFunds(req));
        verify(accountRepository, never()).save(any(Account.class));
    }
}