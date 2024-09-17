package com.dws.challenge.domain;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Account {

	@NotNull
	@NotEmpty
	private final String accountId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;

	@JsonIgnore
	private final ReentrantLock lock = new ReentrantLock();

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}
	
	

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId, @JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}

	public String getAccountId() {
		return accountId;
	}

	public BigDecimal getBalance() {
		return balance;
	}


	public void lock() {
		lock.lock();
	}

	// Unlock the account after operation
	public void unlock() {
		lock.unlock();
	}

	// Method to debit amount (reduce balance)
	public void debit(BigDecimal amount) {
		balance = balance.subtract(amount);
	}

	// Method to credit amount (increase balance)
	public void credit(BigDecimal amount) {
		balance = balance.add(amount);
	}

	// Check if the balance allows for the withdrawal
	public boolean hasSufficientFunds(BigDecimal amount) {
		return balance.compareTo(amount) >= 0;
	}
	
	
}
