package com.dws.challenge.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.repository.AccountsRepository;

@Service
public class AccountsServiceImpl implements AccountsService {

	@Autowired
	private AccountsRepository accountsRepository;

	@Autowired
	private NotificationService notificationService;

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transferMoney(Account accountFrom, Account accountTo, BigDecimal amount)
			throws InsufficientFundsException {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Transfer amount must be positive.");
		}

		// Check if both accounts exist
		if (!accountsRepository.accountExists(accountFrom.getAccountId())) {
			throw new IllegalArgumentException("Source account does not exist: " + accountFrom.getAccountId());
		}

		if (!accountsRepository.accountExists(accountTo.getAccountId())) {
			throw new IllegalArgumentException("Destination account does not exist: " + accountTo.getAccountId());
		}

		// Lock accounts in a consistent order to avoid deadlocks
		Account firstLock = accountFrom.getAccountId().compareTo(accountTo.getAccountId()) < 0 ? accountFrom
				: accountTo;
		Account secondLock = firstLock == accountFrom ? accountTo : accountFrom;

		firstLock.lock();
		try {
			secondLock.lock();
			try {
				Account accountSender = accountsRepository.getAccount(accountFrom.getAccountId());
				Account accountReceiver = accountsRepository.getAccount(accountTo.getAccountId());
				if (!accountSender.hasSufficientFunds(amount)) {
					throw new InsufficientFundsException(
							"Insufficient funds for account: " + accountFrom.getAccountId());
				}

				// Perform the transfer
				accountSender.debit(amount);
				accountReceiver.credit(amount);

				// Send notifications
				// Notify both account holders about the transfer
				notificationService.notifyAboutTransfer(accountFrom,
						"Transferred " + amount + " to account " + accountTo.getAccountId());
				notificationService.notifyAboutTransfer(accountTo,
						"Received " + amount + " from account " + accountFrom.getAccountId());

			} finally {
				secondLock.unlock();
			}
		} finally {
			firstLock.unlock();
		}
	}

}
