package com.skillbox.data.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Account implements AccountInfo, BalanceOperations, AccountStatement {

    private final int accountId;
    private final int userId;
    private final AccountType accountType;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(int accountId, AccountType accountType, int userId) {
        this.accountId = accountId;
        this.accountType = Objects.requireNonNull(accountType, "Тип счета не может быть null");
        this.userId = userId;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public AccountType getAccountType() {
        return accountType;
    }

    @Override
    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::getEffectiveAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(Transaction.MONEY_SCALE, Transaction.MONEY_ROUNDING);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        transactions.add(transaction);
    }

    @Override
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    @Override
    public String toString() {
        return "Счет %d (%s), пользователь %d".formatted(accountId, accountType, userId);
    }
}