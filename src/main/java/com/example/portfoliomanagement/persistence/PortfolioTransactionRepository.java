package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;

import java.util.List;

public class PortfolioTransactionRepository {
    public List<PortfolioTransaction> findAll() {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select transaction
                            from PortfolioTransaction transaction
                            left join fetch transaction.instrument
                            left join fetch transaction.cashAccount
                            left join fetch transaction.targetCashAccount
                            order by transaction.transactionDate, transaction.id
                            """, PortfolioTransaction.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

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
