package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioTransactionRepository {
    public List<PortfolioTransaction> findByInstrumentId(Long instrumentId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select transaction
                            from PortfolioTransaction transaction
                            where transaction.instrument.id = :instrumentId
                            order by transaction.transactionDate desc, transaction.id desc
                            """, PortfolioTransaction.class)
                    .setParameter("instrumentId", instrumentId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<PortfolioTransaction> findByCashAccountId(Long cashAccountId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select transaction
                            from PortfolioTransaction transaction
                            left join fetch transaction.cashAccount
                            left join fetch transaction.targetCashAccount
                            where transaction.cashAccount.id = :cashAccountId
                               or transaction.targetCashAccount.id = :cashAccountId
                            order by transaction.transactionDate desc, transaction.id desc
                            """, PortfolioTransaction.class)
                    .setParameter("cashAccountId", cashAccountId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public BigDecimal calculateCashAccountBalance(Long cashAccountId) {
        return findByCashAccountId(cashAccountId).stream()
                .map(transaction -> cashEffect(cashAccountId, transaction))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal cashEffect(Long cashAccountId, PortfolioTransaction transaction) {
        BigDecimal amount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount();
        BigDecimal fees = transaction.getFees() == null ? BigDecimal.ZERO : transaction.getFees();
        BigDecimal taxes = transaction.getTaxes() == null ? BigDecimal.ZERO : transaction.getTaxes();

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

    public List<PortfolioTransaction> findBySecurityAccountId(Long securityAccountId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select transaction
                            from PortfolioTransaction transaction
                            left join fetch transaction.instrument
                            left join fetch transaction.cashAccount
                            where transaction.securityAccount.id = :securityAccountId
                            order by transaction.transactionDate desc, transaction.id desc
                            """, PortfolioTransaction.class)
                    .setParameter("securityAccountId", securityAccountId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }
}
