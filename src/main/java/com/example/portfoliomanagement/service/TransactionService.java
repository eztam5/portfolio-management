package com.example.portfoliomanagement.service;

import com.example.portfoliomanagement.persistence.CashAccount;
import com.example.portfoliomanagement.persistence.Instrument;
import com.example.portfoliomanagement.persistence.PersistenceManager;
import com.example.portfoliomanagement.persistence.PortfolioTransaction;
import com.example.portfoliomanagement.persistence.SecurityAccount;
import com.example.portfoliomanagement.persistence.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionService {
    public PortfolioTransaction deposit(
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal amount,
            String currency,
            String note) {
        return save(new TransactionDraft(
                TransactionType.DEPOSIT,
                transactionDate,
                cashAccountId,
                null,
                null,
                null,
                null,
                amount,
                currency,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                note));
    }

    public PortfolioTransaction withdrawal(
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal amount,
            String currency,
            String note) {
        return save(new TransactionDraft(
                TransactionType.WITHDRAWAL,
                transactionDate,
                cashAccountId,
                null,
                null,
                null,
                null,
                amount,
                currency,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                note));
    }

    public PortfolioTransaction transfer(
            Long sourceCashAccountId,
            Long targetCashAccountId,
            LocalDate transactionDate,
            BigDecimal amount,
            String currency,
            String note) {
        return save(new TransactionDraft(
                TransactionType.CASH_TRANSFER,
                transactionDate,
                sourceCashAccountId,
                targetCashAccountId,
                null,
                null,
                null,
                amount,
                currency,
                null,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                note));
    }

    public PortfolioTransaction buy(
            Long instrumentId,
            Long securityAccountId,
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        return securityTransaction(
                TransactionType.BUY,
                instrumentId,
                securityAccountId,
                cashAccountId,
                transactionDate,
                shares,
                amount,
                currency,
                fees,
                taxes,
                note);
    }

    public PortfolioTransaction sell(
            Long instrumentId,
            Long securityAccountId,
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        return securityTransaction(
                TransactionType.SELL,
                instrumentId,
                securityAccountId,
                cashAccountId,
                transactionDate,
                shares,
                amount,
                currency,
                fees,
                taxes,
                note);
    }

    public PortfolioTransaction dividend(
            Long instrumentId,
            Long securityAccountId,
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal amount,
            String currency,
            BigDecimal taxes,
            String note) {
        return save(new TransactionDraft(
                TransactionType.DIVIDEND,
                transactionDate,
                cashAccountId,
                null,
                securityAccountId,
                instrumentId,
                null,
                amount,
                currency,
                amount,
                currency,
                null,
                BigDecimal.ZERO,
                taxes,
                note));
    }

    public void update(
            Long transactionId,
            TransactionType type,
            LocalDate transactionDate,
            Long cashAccountId,
            Long targetCashAccountId,
            Long securityAccountId,
            Long instrumentId,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            PortfolioTransaction portfolioTransaction = entityManager.find(PortfolioTransaction.class, transactionId);
            if (portfolioTransaction == null) {
                throw new IllegalArgumentException("Transaction does not exist: " + transactionId);
            }

            BigDecimal grossAmount = type == TransactionType.DIVIDEND || type == TransactionType.BUY || type == TransactionType.SELL
                    ? amount
                    : null;
            String grossAmountCurrency = grossAmount == null ? null : currency;
            portfolioTransaction.update(
                    type,
                    transactionDate,
                    reference(entityManager, CashAccount.class, cashAccountId),
                    reference(entityManager, CashAccount.class, targetCashAccountId),
                    reference(entityManager, SecurityAccount.class, securityAccountId),
                    reference(entityManager, Instrument.class, instrumentId),
                    shares,
                    amount,
                    currency,
                    grossAmount,
                    grossAmountCurrency,
                    null,
                    fees,
                    taxes,
                    note);
            transaction.commit();
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private PortfolioTransaction securityTransaction(
            TransactionType type,
            Long instrumentId,
            Long securityAccountId,
            Long cashAccountId,
            LocalDate transactionDate,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        return save(new TransactionDraft(
                type,
                transactionDate,
                cashAccountId,
                null,
                securityAccountId,
                instrumentId,
                shares,
                amount,
                currency,
                amount,
                currency,
                null,
                fees,
                taxes,
                note));
    }

    private PortfolioTransaction save(TransactionDraft draft) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            PortfolioTransaction portfolioTransaction = new PortfolioTransaction(
                    draft.type(),
                    draft.transactionDate(),
                    reference(entityManager, CashAccount.class, draft.cashAccountId()),
                    reference(entityManager, CashAccount.class, draft.targetCashAccountId()),
                    reference(entityManager, SecurityAccount.class, draft.securityAccountId()),
                    reference(entityManager, Instrument.class, draft.instrumentId()),
                    draft.shares(),
                    draft.amount(),
                    draft.currency(),
                    draft.grossAmount(),
                    draft.grossAmountCurrency(),
                    draft.exchangeRate(),
                    draft.fees(),
                    draft.taxes(),
                    draft.note());
            entityManager.persist(portfolioTransaction);
            transaction.commit();
            return portfolioTransaction;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private <T> T reference(EntityManager entityManager, Class<T> entityClass, Long id) {
        if (id == null) {
            return null;
        }
        return entityManager.getReference(entityClass, id);
    }

    private record TransactionDraft(
            TransactionType type,
            LocalDate transactionDate,
            Long cashAccountId,
            Long targetCashAccountId,
            Long securityAccountId,
            Long instrumentId,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal grossAmount,
            String grossAmountCurrency,
            BigDecimal exchangeRate,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
    }
}
