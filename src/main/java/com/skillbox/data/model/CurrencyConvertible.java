package com.skillbox.data.model;

import java.math.BigDecimal;

public interface CurrencyConvertible {

    BigDecimal convertToBaseCurrency(BigDecimal amount);

}


