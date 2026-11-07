package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class ForeignCurrencyTransaction extends Transaction implements CurrencyConvertible {

    private final BigDecimal exchangeRate;

    public ForeignCurrencyTransaction(int accountId,
                                      int transactionId,
                                      LocalDateTime dateTime,
                                      String category,
                                      BigDecimal amount,
                                      BigDecimal exchangeRate) {
        super(accountId, transactionId, dateTime, category, amount);
        this.exchangeRate = Objects.requireNonNull(exchangeRate, "Курс валюты не может быть null");
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    @Override
    public BigDecimal convertToBaseCurrency(BigDecimal amount) {
        return scale(amount.multiply(exchangeRate));
    }

    @Override
    public BigDecimal getEffectiveAmount() {
        return convertToBaseCurrency(getAmount());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.FOREIGN_CURRENCY;
    }
}