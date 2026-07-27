package com.example.portfoliomanagement.calculation;

import com.example.portfoliomanagement.persistence.PortfolioTransaction;

import java.math.BigDecimal;
import java.util.List;

public class CashBalanceCalculator {
    public BigDecimal calculate(Long cashAccountId, List<PortfolioTransaction> transactions) {
        return transactions.stream()
                .map(transaction -> cashEffect(cashAccountId, transaction))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal cashEffect(Long cashAccountId, PortfolioTransaction transaction) {
        BigDecimal amount = zeroIfNull(transaction.getAmount());
        BigDecimal fees = zeroIfNull(transaction.getFees());
        BigDecimal taxes = zeroIfNull(transaction.getTaxes());

        return switch (transaction.getType()) {
            case DEPOSIT -> amount;
            case WITHDRAWAL -> amount.negate();
            case CASH_TRANSFER -> {
                if (transaction.getCashAccount() != null && cashAccountId.equals(transaction.getCashAccount().getId())) {
                    yield amount.negate();
                }
                yield amount;
            }
            case BUY -> amount.add(fees).add(taxes).negate();
            case SELL -> amount.subtract(fees).subtract(taxes);
            case DIVIDEND -> amount.subtract(taxes);
        };
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
