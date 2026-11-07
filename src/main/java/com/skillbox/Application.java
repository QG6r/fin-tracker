package com.skillbox;


import com.skillbox.controller.MainMenuController;
import com.skillbox.data.repository.AccountRepository;
import com.skillbox.data.repository.AnalyticRepository;
import com.skillbox.data.repository.FileAccountRepository;
import com.skillbox.data.repository.FileTransactionRepository;
import com.skillbox.data.repository.JsonAnalyticRepository;
import com.skillbox.data.repository.TransactionRepository;
import com.skillbox.exception.ApplicationException;
import com.skillbox.exception.InvalidArgumentsException;
import com.skillbox.service.TransactionService;
import com.skillbox.service.TransactionServiceImpl;

public class Application {

    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                throw new InvalidArgumentsException(
                        "Необходимо указать имена файлов для входных данных аккаунтов и транзакций, а также для выходного файла.");
            }
            String accountFilename = args[0];
            String transactionFilename = args[1];
            String outputFilename = args[2];

            AccountRepository accountReader = new FileAccountRepository(accountFilename);
            TransactionRepository transactionReader = new FileTransactionRepository(transactionFilename);
            TransactionService transactionService = new TransactionServiceImpl(transactionReader, accountReader);
            AnalyticRepository saver = new JsonAnalyticRepository(outputFilename);

            new MainMenuController(transactionService, saver).start();
        } catch (ApplicationException exception) {
            System.err.println("Ошибка приложения: " + exception.getMessage());
        }
    }
}