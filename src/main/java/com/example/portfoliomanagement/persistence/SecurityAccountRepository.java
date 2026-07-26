package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class SecurityAccountRepository {
    public List<SecurityAccount> findAll() {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery(
                            "select securityAccount from SecurityAccount securityAccount order by securityAccount.name",
                            SecurityAccount.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public SecurityAccount save(String name, String note) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            SecurityAccount securityAccount = new SecurityAccount(name, note);
            entityManager.persist(securityAccount);
            transaction.commit();
            return securityAccount;
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
