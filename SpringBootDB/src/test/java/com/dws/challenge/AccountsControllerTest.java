package com.dws.challenge;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.AccountsServiceImpl;
import com.dws.challenge.web.AccountsController;
import com.fasterxml.jackson.databind.ObjectMapper;

class AccountsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountsServiceImpl accountsService;

    @InjectMocks
    private AccountsController accountsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(accountsController).build();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        Account account = new Account("123", new BigDecimal("1000.00"));

        // Act & Assert
        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateAccount_DuplicateAccount() throws Exception {
        Account account = new Account("123", new BigDecimal("1000.00"));

        // Simulate duplicate account exception
        doThrow(new DuplicateAccountIdException("Account id 123 already exists!"))
                .when(accountsService).createAccount(account);

        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(account)))
               .andExpect(status().isBadRequest())
                .andExpect(content().string("Account id 123 already exists!"));
    }

    @Test
    void testGetAccount_Success() throws Exception {
        Account account = new Account("123", new BigDecimal("1000.00"));
        
        // Simulate account retrieval
        when(accountsService.getAccount("123")).thenReturn(account);

        mockMvc.perform(get("/v1/accounts/123"))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(account)));
    }

    @Test
    void testTransferMoney_Success() throws Exception {
        TransferRequest transferRequest = new TransferRequest(
                new Account("123", new BigDecimal("1000.00")),
                new Account("456", new BigDecimal("500.00")),
                new BigDecimal("100.00"));

        mockMvc.perform(post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful."));
    }

    @Test
    void testTransferMoney_InsufficientFunds() throws Exception {
        TransferRequest transferRequest = new TransferRequest(
                new Account("123", new BigDecimal("1000.00")),
                new Account("456", new BigDecimal("500.00")),
                new BigDecimal("1500.00"));  // More than available balance

        doThrow(new InsufficientFundsException("Insufficient funds for account: 123"))
                .when(accountsService).transferMoney(transferRequest.getAccountFrom(),
                        transferRequest.getAccountTo(), transferRequest.getAmount());

        mockMvc.perform(post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds for account: 123"));
    }

    @Test
    void testTransferMoney_AccountDoesNotExist() throws Exception {
        TransferRequest transferRequest = new TransferRequest(
                new Account("123", new BigDecimal("1000.00")),
                new Account("456", new BigDecimal("500.00")),
                new BigDecimal("100.00"));

        doThrow(new IllegalArgumentException("Source account does not exist: 123"))
                .when(accountsService).transferMoney(transferRequest.getAccountFrom(),
                        transferRequest.getAccountTo(), transferRequest.getAmount());

        mockMvc.perform(post("/v1/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Source account does not exist: 123"));
    }

    // Utility to convert objects to JSON strings
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
