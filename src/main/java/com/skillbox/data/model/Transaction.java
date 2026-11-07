package com.skillbox.data.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Transaction {

    public static final int MONEY_SCALE = 2;

    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final int accountId;
    private final int transactionId;
    private final LocalDateTime dateTime;
    private final String category;
    private final BigDecimal amount;

    protected Transaction(int accountId,
                          int transactionId,
                          LocalDateTime dateTime,
                          String category,
                          BigDecimal amount) {
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.dateTime = Objects.requireNonNull(dateTime, "Дата транзакции не может быть null");
        this.category = category == null ? "" : category;
        this.amount = Objects.requireNonNull(amount, "Сумма транзакции не может быть null");
    }

    public int getAccountId() {
        return accountId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getEffectiveAmount() {
        return scale(amount);
    }

    public abstract TransactionType getType();

    protected static BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }

    @Override
    public String toString() {
        return "%d [%s] %s, %s: %s".formatted(transactionId, getType().getName(), dateTime, category,
                getEffectiveAmount().toPlainString());
    }
}