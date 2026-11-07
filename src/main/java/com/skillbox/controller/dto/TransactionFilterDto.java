package com.skillbox.controller.dto;

import com.skillbox.data.model.Commentable;
import com.skillbox.data.model.Recurring;
import com.skillbox.data.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TransactionFilterDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private String category;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comment;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private Predicate<Transaction> datePredicate() {
        if (startDate == null && endDate == null) {
            return transaction -> true;
        }
        return transaction -> {
            LocalDateTime date = transaction.getDateTime();
            LocalDateTime start = startDate == null ? null : startDate.atStartOfDay();
            LocalDateTime end = endDate == null ? null : endDate.atTime(LocalTime.MAX);
            boolean inRange = (start == null || !date.isBefore(start))
                    && (end == null || !date.isAfter(end));
            return inRange
                    || (transaction instanceof Recurring recurring && recurring.isExecutedBetween(start, end));
        };
    }

    private Predicate<Transaction> commentPredicate() {
        if (comment == null || comment.isBlank()) {
            return transaction -> true;
        }
        String token = comment.trim().toLowerCase();
        return transaction -> transaction instanceof Commentable commentable
                && commentable.getComments().stream()
                .anyMatch(value -> value.toLowerCase().contains(token));
    }

    private Predicate<Transaction> amountPredicate() {
        if (minAmount == null && maxAmount == null) {
            return transaction -> true;
        }
        return transaction -> {
            BigDecimal amount = transaction.getEffectiveAmount();
            return (minAmount == null || amount.compareTo(minAmount) >= 0)
                    && (maxAmount == null || amount.compareTo(maxAmount) <= 0);
        };
    }

    private Predicate<Transaction> categoryPredicate() {
        if (category == null || category.isBlank()) {
            return transaction -> true;
        }
        String value = category.trim();
        return transaction -> value.equals(transaction.getCategory());
    }

    public Predicate<Transaction> buildPredicate() {
        return categoryPredicate()
                .and(amountPredicate())
                .and(commentPredicate())
                .and(datePredicate());
    }

    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (category != null && !category.isBlank()) {
            parts.add("категория — " + category);
        }
        if (startDate != null) {
            parts.add("начальная дата — " + startDate);
        }
        if (endDate != null) {
            parts.add("конечная дата — " + endDate);
        }
        if (minAmount != null) {
            parts.add("минимальная сумма — " + minAmount.toPlainString());
        }
        if (maxAmount != null) {
            parts.add("максимальная сумма — " + maxAmount.toPlainString());
        }
        if (comment != null && !comment.isBlank()) {
            parts.add("комментарий — " + comment);
        }
        return String.join(", ", parts);
    }
}