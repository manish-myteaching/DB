package com.dws.challenge.service;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;

public interface AccountsService {

	public void createAccount(Account account);

	public Account getAccount(String accountId);

	public void transferMoney(Account accountFrom, Account accountTo, BigDecimal amount)
			throws InsufficientFundsException;

}
