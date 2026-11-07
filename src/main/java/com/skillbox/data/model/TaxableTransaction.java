package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class TaxableTransaction extends Transaction implements Taxable {

    private final BigDecimal taxRate;

    public TaxableTransaction(int accountId,
                              int transactionId,
                              LocalDateTime dateTime,
                              String category,
                              BigDecimal amount,
                              BigDecimal taxRate) {
        super(accountId, transactionId, dateTime, category, amount);
        this.taxRate = Objects.requireNonNull(taxRate, "Ставка налога не может быть null");
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    @Override
    public BigDecimal calculateTax() {
        return scale(getAmount().multiply(taxRate));
    }

    @Override
    public BigDecimal getEffectiveAmount() {
        return scale(getAmount().subtract(calculateTax()));
    }

    @Override
    public TransactionType getType() {
        return TransactionType.TAXABLE;
    }
}