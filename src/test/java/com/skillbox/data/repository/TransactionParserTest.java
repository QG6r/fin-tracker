package com.skillbox.data.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skillbox.data.model.CommentableTransaction;
import com.skillbox.data.model.ForeignCurrencyTransaction;
import com.skillbox.data.model.RecurrencePattern;
import com.skillbox.data.model.RecurringTransaction;
import com.skillbox.data.model.RegularTransaction;
import com.skillbox.data.model.TaxableTransaction;
import com.skillbox.data.model.Transaction;
import com.skillbox.exception.DataParseException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TransactionParserTest {

    @Test
    void parsesARegularTransactionWithAnEmptyExtraField() {
        Transaction transaction = TransactionParser.parse("10001,2,2020-01-01T12:00:00,Food,-25.50,Regular,");

        assertThat(transaction).isInstanceOf(RegularTransaction.class);
        assertThat(transaction.getAccountId()).isEqualTo(10001);
        assertThat(transaction.getTransactionId()).isEqualTo(2);
        assertThat(transaction.getDateTime()).isEqualTo(LocalDateTime.of(2020, 1, 1, 12, 0));
        assertThat(transaction.getCategory()).isEqualTo("Food");
        assertThat(transaction.getAmount()).isEqualByComparingTo("-25.50");
    }

    @Test
    void theTrailingCommaIsOptional() {
        assertThat(TransactionParser.parse("10001,2,2020-01-01T12:00:00,Food,-25.50,Regular"))
                .isInstanceOf(RegularTransaction.class);
    }

    @Test
    void parsesATaxableTransaction() {
        TaxableTransaction transaction = (TaxableTransaction) TransactionParser
                .parse("10002,8,2020-04-15T14:15:00,Salary,5000.00,Taxable,0.20");

        assertThat(transaction.getTaxRate()).isEqualByComparingTo("0.20");
        assertThat(transaction.getEffectiveAmount()).isEqualByComparingTo("4000.00");
    }

    @Test
    void parsesAForeignCurrencyTransaction() {
        ForeignCurrencyTransaction transaction = (ForeignCurrencyTransaction) TransactionParser
                .parse("10001,6,2020-03-22T19:00:00,Entertainment,-45.00,ForeignCurrency,85");

        assertThat(transaction.getExchangeRate()).isEqualByComparingTo("85");
    }

    @Test
    void parsesACommentableTransactionAndTrimsItsComments() {
        CommentableTransaction transaction = (CommentableTransaction) TransactionParser
                .parse("10001,9,2023-02-14T20:00:00,Entertainment,-60.00,Commentable,Dinner; Valentine's day ;");

        assertThat(transaction.getComments()).containsExactly("Dinner", "Valentine's day");
    }

    @Test
    void parsesARecurringTransaction() {
        RecurringTransaction transaction = (RecurringTransaction) TransactionParser
                .parse("10001,3,2023-02-01T10:00:00,Rent,-800.00,Recurrent,monthly;2");

        assertThat(transaction.getPattern()).isEqualTo(RecurrencePattern.MONTHLY);
        assertThat(transaction.getRepeatCount()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1,2,3",
            "abc,2,2020-01-01T12:00:00,Food,-25.50,Regular,",
            "1,2,not-a-date,Food,-25.50,Regular,",
            "1,2,2020-01-01T12:00:00,Food,lots,Regular,",
            "1,2,2020-01-01T12:00:00,Food,-25.50,Unknown,",
            "1,2,2020-01-01T12:00:00,Salary,100.00,Taxable,",
            "1,2,2020-01-01T12:00:00,Travel,100.00,ForeignCurrency,",
            "1,2,2020-01-01T12:00:00,Rent,100.00,Recurrent,monthly",
            "1,2,2020-01-01T12:00:00,Rent,100.00,Recurrent,fortnightly;2",
            "1,2,2020-01-01T12:00:00,Rent,100.00,Recurrent,monthly;many"
    })
    void rejectsMalformedLinesWithAParseException(String line) {
        assertThatThrownBy(() -> TransactionParser.parse(line)).isInstanceOf(DataParseException.class);
    }
}
