package com.skillbox.data.repository;

import com.skillbox.data.model.Transaction;
import java.util.List;

public interface TransactionRepository {

    List<Transaction> readAll();
}
