package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class InstrumentListRepository {
    public List<InstrumentList> findAll() {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery(
                            "select instrumentList from InstrumentList instrumentList order by instrumentList.name",
                            InstrumentList.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public InstrumentList save(String name) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            InstrumentList instrumentList = new InstrumentList(name);
            entityManager.persist(instrumentList);
            transaction.commit();
            return instrumentList;
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
