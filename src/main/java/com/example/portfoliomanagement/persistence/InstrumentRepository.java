package com.example.portfoliomanagement.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;

public class InstrumentRepository {
    public List<Instrument> findAll() {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery(
                            "select instrument from Instrument instrument order by instrument.name",
                            Instrument.class)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public List<Instrument> findByListId(Long listId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select instrument
                            from InstrumentList instrumentList
                            join instrumentList.instruments instrument
                            where instrumentList.id = :listId
                            order by instrument.name
                            """, Instrument.class)
                    .setParameter("listId", listId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public Instrument save(String name, String symbol, String isin, BigDecimal latest, String currency) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Instrument instrument = new Instrument(name, symbol, isin, latest, currency);
            entityManager.persist(instrument);
            transaction.commit();
            return instrument;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public Instrument saveToList(
            Long listId,
            String name,
            String symbol,
            String isin,
            BigDecimal latest,
            String currency) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            InstrumentList instrumentList = entityManager.find(InstrumentList.class, listId);
            if (instrumentList == null) {
                throw new IllegalArgumentException("Instrument list does not exist: " + listId);
            }

            Instrument instrument = new Instrument(name, symbol, isin, latest, currency);
            entityManager.persist(instrument);
            instrumentList.getInstruments().add(instrument);
            transaction.commit();
            return instrument;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public void addToList(Long instrumentId, Long listId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Instrument instrument = entityManager.find(Instrument.class, instrumentId);
            InstrumentList instrumentList = entityManager.find(InstrumentList.class, listId);
            if (instrument == null) {
                throw new IllegalArgumentException("Instrument does not exist: " + instrumentId);
            }
            if (instrumentList == null) {
                throw new IllegalArgumentException("Instrument list does not exist: " + listId);
            }

            instrumentList.getInstruments().add(instrument);
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

    public void removeFromList(Long instrumentId, Long listId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Instrument instrument = entityManager.find(Instrument.class, instrumentId);
            InstrumentList instrumentList = entityManager.find(InstrumentList.class, listId);
            if (instrument == null) {
                throw new IllegalArgumentException("Instrument does not exist: " + instrumentId);
            }
            if (instrumentList == null) {
                throw new IllegalArgumentException("Instrument list does not exist: " + listId);
            }

            instrumentList.getInstruments().remove(instrument);
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

    public void delete(Long instrumentId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Instrument instrument = entityManager.find(Instrument.class, instrumentId);
            if (instrument != null) {
                entityManager.remove(instrument);
            }
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
}
