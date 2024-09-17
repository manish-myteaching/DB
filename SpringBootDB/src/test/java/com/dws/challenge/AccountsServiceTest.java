package com.dws.challenge;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsServiceImpl;
import com.dws.challenge.service.NotificationService;


class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AccountsServiceImpl accountsService;

    private Account accountFrom;
    private Account accountTo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountFrom = new Account("123", new BigDecimal("100.00"));
        accountTo = new Account("456", new BigDecimal("50.00"));
    }

    @Test
    void testTransferMoney_Success() throws InsufficientFundsException {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("50.00");
        
        when(accountsRepository.accountExists(accountFrom.getAccountId())).thenReturn(true);
        when(accountsRepository.accountExists(accountTo.getAccountId())).thenReturn(true);
        when(accountsRepository.getAccount(accountFrom.getAccountId())).thenReturn(accountFrom);
        when(accountsRepository.getAccount(accountTo.getAccountId())).thenReturn(accountTo);
        
        // Act
        accountsService.transferMoney(accountFrom, accountTo, transferAmount);
        
        // Assert
        verify(notificationService, times(1)).notifyAboutTransfer(accountFrom, "Transferred 50.00 to account 456");
        verify(notificationService, times(1)).notifyAboutTransfer(accountTo, "Received 50.00 from account 123");
        verify(accountsRepository, times(1)).getAccount(accountFrom.getAccountId());
        verify(accountsRepository, times(1)).getAccount(accountTo.getAccountId());
    }

    @Test
    void testTransferMoney_InsufficientFunds() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("150.00");
        
        when(accountsRepository.accountExists(accountFrom.getAccountId())).thenReturn(true);
        when(accountsRepository.accountExists(accountTo.getAccountId())).thenReturn(true);
        when(accountsRepository.getAccount(accountFrom.getAccountId())).thenReturn(accountFrom);
        when(accountsRepository.getAccount(accountTo.getAccountId())).thenReturn(accountTo);
        
        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            accountsService.transferMoney(accountFrom, accountTo, transferAmount);
        });
    }

    @Test
    void testTransferMoney_SourceAccountDoesNotExist() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("50.00");
        
        when(accountsRepository.accountExists(accountFrom.getAccountId())).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            accountsService.transferMoney(accountFrom, accountTo, transferAmount);
        });
    }

    @Test
    void testTransferMoney_DestinationAccountDoesNotExist() {
        // Arrange
        BigDecimal transferAmount = new BigDecimal("50.00");
        
        when(accountsRepository.accountExists(accountFrom.getAccountId())).thenReturn(true);
        when(accountsRepository.accountExists(accountTo.getAccountId())).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            accountsService.transferMoney(accountFrom, accountTo, transferAmount);
        });
    }
}
