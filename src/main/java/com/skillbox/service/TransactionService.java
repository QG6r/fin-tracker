package com.skillbox.service;

import com.skillbox.controller.option.AggregateOption;
import com.skillbox.controller.option.GroupOption;
import com.skillbox.controller.dto.TransactionFilterDto;
import com.skillbox.data.model.Analytic;

public interface TransactionService {

    Analytic calculateAnalytics(TransactionFilterDto transactionFilter,
                                GroupOption groupOption,
                                AggregateOption aggregateOption);
}
