package com.skillbox.data.repository;

import com.skillbox.data.model.Account;
import java.util.List;

public interface AccountRepository {

    List<Account> readAll();
}
