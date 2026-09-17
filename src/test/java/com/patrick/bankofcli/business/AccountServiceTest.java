package com.patrick.bankofcli.business;

import com.patrick.bankofcli.exception.InsufficientFundsException;
import com.patrick.bankofcli.model.Account;
import com.patrick.bankofcli.repository.AccountRepository;
import com.patrick.bankofcli.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private Connection connection;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionRepository);
    }

    @Test
    void withdraw_withSufficientFunds_reducesBalanceSuccessfully() throws SQLException {
        // Arrange
        Account account = new Account(1, "1234", new BigDecimal("100.00"));
        when(accountRepository.findById(connection, 1)).thenReturn(Optional.of(account));
        when(accountRepository.updateBalance(eq(connection), eq(1), any(BigDecimal.class)))
                .thenReturn(true);

        // Act
        Account result = accountService.withdraw(connection, 1, new BigDecimal("30.00"));

        // Assert
        assertEquals(new BigDecimal("70.00"), result.getBalance());
        verify(accountRepository).updateBalance(connection, 1, new BigDecimal("70.00"));
        // Currently just verifies any type of transaction occurs
        verify(transactionRepository).create(eq(connection), any());
    }

    @Test
    void withdraw_withInsufficientFunds_throwsExceptionAndDoesNotUpdateBalance() throws SQLException{
        // Arrange
        Account account = new Account(1, "1234", new BigDecimal("20.00"));
        when(accountRepository.findById(connection, 1)).thenReturn(Optional.of(account));

        // Act + Assert
        assertThrows(InsufficientFundsException.class, () ->
                accountService.withdraw(connection, 1, new BigDecimal("60.00")));

        verify(accountRepository, never()).updateBalance(any(), anyInt(), any());
        verify(transactionRepository, never()).create(any(), any());
    }
}
