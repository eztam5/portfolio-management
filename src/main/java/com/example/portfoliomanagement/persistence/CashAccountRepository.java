package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;

public class CashAccountRepository {
    public List<CashAccount> findAll() {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery(
                            "select cashAccount from CashAccount cashAccount order by cashAccount.name",
                            CashAccount.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public CashAccount save(String name, String currency, BigDecimal balance, String note) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            CashAccount cashAccount = new CashAccount(name, currency, balance, note);
            entityManager.persist(cashAccount);
            transaction.commit();
            return cashAccount;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }
}
