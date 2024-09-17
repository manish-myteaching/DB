package com.dws.challenge.domain;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferRequest {

    @NotNull(message = "Source account must not be null")
    private Account accountFrom;

    @NotNull(message = "Destination account must not be null")
    private Account accountTo;

    @NotNull(message = "Transfer amount must not be null")
    @Min(value = 1, message = "Transfer amount must be greater than 0")
    private BigDecimal amount;
    
    
}
