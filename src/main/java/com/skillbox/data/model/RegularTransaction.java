package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegularTransaction extends Transaction {

    public RegularTransaction(int accountId,
                              int transactionId,
                              LocalDateTime dateTime,
                              String category,
                              BigDecimal amount) {
        super(accountId, transactionId, dateTime, category, amount);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.REGULAR;
    }
}