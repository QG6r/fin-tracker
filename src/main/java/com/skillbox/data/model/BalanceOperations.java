package com.skillbox.data.model;

import java.math.BigDecimal;

public interface BalanceOperations {

    BigDecimal getBalance();

    void addTransaction(Transaction transaction);
}
