package com.skillbox.data.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.skillbox.data.model.Account;
import com.skillbox.data.model.AccountType;
import com.skillbox.data.model.Transaction;
import com.skillbox.exception.DataParseException;
import com.skillbox.exception.DataReadException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileRepositoriesTest {

    @TempDir
    Path directory;

    private Path write(String name, String content) throws IOException {
        return Files.writeString(directory.resolve(name), content, StandardCharsets.UTF_8);
    }

    @Test
    void readsAccountsAndIgnoresCommentsAndBlankLines() throws IOException {
        Path file = write("accounts.txt", """
                # id, type, user
                10001,0,201

                10002,2,201 # credit card
                """);

        List<Account> accounts = new FileAccountRepository(file.toString()).readAll();

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).getAccountId()).isEqualTo(10001);
        assertThat(accounts.get(0).getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(accounts.get(1).getAccountType()).isEqualTo(AccountType.CREDIT);
        assertThat(accounts.get(1).getUserId()).isEqualTo(201);
    }

    @Test
    void rejectsAnUnknownAccountTypeAndGarbage() throws IOException {
        Path unknownType = write("unknown.txt", "10001,9,201\n");
        Path garbage = write("garbage.txt", "ten,0,201\n");
        Path tooShort = write("short.txt", "10001,0\n");

        assertThatThrownBy(() -> new FileAccountRepository(unknownType.toString()).readAll())
                .isInstanceOf(DataParseException.class);
        assertThatThrownBy(() -> new FileAccountRepository(garbage.toString()).readAll())
                .isInstanceOf(DataParseException.class);
        assertThatThrownBy(() -> new FileAccountRepository(tooShort.toString()).readAll())
                .isInstanceOf(DataParseException.class);
    }

    @Test
    void readsTransactionsOfEveryTypeAndIgnoresComments() throws IOException {
        Path file = write("transactions.txt", """
                # account, id, date, category, amount, type, extra
                10001,1,2023-01-05T09:00:00,Salary,3000.00,Taxable,0.13
                10001,2,2023-01-10T18:30:00,Food,-42.50,Regular,
                10001,3,2023-02-01T10:00:00,Rent,-800.00,Recurrent,monthly;2 # rent
                10001,4,2023-02-14T20:00:00,Fun,-60.00,Commentable,Dinner;Valentine's day
                10002,5,2023-03-03T12:00:00,Travel,-120.00,ForeignCurrency,0.92
                """);

        List<Transaction> transactions = new FileTransactionRepository(file.toString()).readAll();

        assertThat(transactions).extracting(Transaction::getTransactionId).containsExactly(1, 2, 3, 4, 5);
        assertThat(transactions).extracting(transaction -> transaction.getType().getName())
                .containsExactly("Taxable", "Regular", "Recurrent", "Commentable", "ForeignCurrency");
    }

    @Test
    void aBadTransactionLineFailsTheWholeRead() throws IOException {
        Path file = write("bad.txt", "10001,1,2023-01-05T09:00:00,Food,oops,Regular,\n");

        assertThatThrownBy(() -> new FileTransactionRepository(file.toString()).readAll())
                .isInstanceOf(DataParseException.class);
    }

    @Test
    void missingFilesProduceAReadException() {
        String missing = directory.resolve("missing.txt").toString();

        assertThatThrownBy(() -> new FileAccountRepository(missing).readAll()).isInstanceOf(DataReadException.class);
        assertThatThrownBy(() -> new FileTransactionRepository(missing).readAll()).isInstanceOf(DataReadException.class);
    }
}
