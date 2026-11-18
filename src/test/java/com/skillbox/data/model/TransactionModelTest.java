package com.skillbox.data.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionModelTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2023, 1, 1, 0, 0);

    @Test
    void regularTransactionKeepsItsAmount() {
        Transaction transaction = new RegularTransaction(1, 1, DATE, "Food", new BigDecimal("-25.5"));

        assertThat(transaction.getEffectiveAmount()).isEqualByComparingTo("-25.50");
        assertThat(transaction.getType()).isEqualTo(TransactionType.REGULAR);
    }

    @Test
    void taxableTransactionSubtractsTax() {
        TaxableTransaction transaction = new TaxableTransaction(1, 1, DATE, "Salary",
                new BigDecimal("5000.00"), new BigDecimal("0.20"));

        assertThat(transaction.calculateTax()).isEqualByComparingTo("1000.00");
        assertThat(transaction.getEffectiveAmount()).isEqualByComparingTo("4000.00");
    }

    @Test
    void foreignCurrencyTransactionIsConvertedWithTheExchangeRate() {
        ForeignCurrencyTransaction transaction = new ForeignCurrencyTransaction(1, 1, DATE, "Travel",
                new BigDecimal("-45.00"), new BigDecimal("85"));

        assertThat(transaction.getEffectiveAmount()).isEqualByComparingTo("-3825.00");
    }

    @Test
    void commentableTransactionExposesAnUnmodifiableCopyOfItsComments() {
        CommentableTransaction transaction = new CommentableTransaction(1, 1, DATE, "Gift",
                BigDecimal.TEN, List.of("Birthday", "Present"));

        assertThat(transaction.getComments()).containsExactly("Birthday", "Present");
        assertThat(new CommentableTransaction(1, 2, DATE, "Gift", BigDecimal.TEN, null).getComments()).isEmpty();
    }

    @Test
    void amountsAreRoundedHalfUpToTwoDecimals() {
        Transaction transaction = new RegularTransaction(1, 1, DATE, "Food", new BigDecimal("1.005"));

        assertThat(transaction.getEffectiveAmount()).isEqualByComparingTo("1.01");
        assertThat(transaction.getEffectiveAmount().scale()).isEqualTo(2);
    }

    @Test
    void accountBalanceIsTheSumOfEffectiveAmounts() {
        Account account = new Account(1, AccountType.CHECKING, 100);
        account.addTransaction(new RegularTransaction(1, 1, DATE, "Food", new BigDecimal("-10.00")));
        account.addTransaction(new TaxableTransaction(1, 2, DATE, "Salary", new BigDecimal("100.00"), new BigDecimal("0.10")));
        account.addTransaction(null);

        assertThat(account.getTransactions()).hasSize(2);
        assertThat(account.getBalance()).isEqualByComparingTo("80.00");
    }

    @Test
    void accountTypesAreLookedUpByTheirCode() {
        assertThat(AccountType.of(0)).isEqualTo(AccountType.CHECKING);
        assertThat(AccountType.of(1)).isEqualTo(AccountType.SAVINGS);
        assertThat(AccountType.of(2)).isEqualTo(AccountType.CREDIT);
        assertThat(AccountType.of(7)).isNull();
    }
}
