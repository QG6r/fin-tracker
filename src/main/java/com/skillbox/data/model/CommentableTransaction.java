package com.skillbox.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentableTransaction extends Transaction implements Commentable {

    private final List<String> comments;

    public CommentableTransaction(int accountId,
                                  int transactionId,
                                  LocalDateTime dateTime,
                                  String category,
                                  BigDecimal amount,
                                  List<String> comments) {
        super(accountId, transactionId, dateTime, category, amount);
        this.comments = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
    }

    @Override
    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.COMMENTABLE;
    }
}