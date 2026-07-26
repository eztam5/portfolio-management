package com.example.portfoliomanagement.persistence;

import com.example.portfoliomanagement.marketdata.HistoricalPrice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class InstrumentPriceRepository {
    public List<InstrumentPrice> findByInstrumentId(Long instrumentId) {
        EntityManager entityManager = PersistenceManager.createEntityManager();

        try {
            return entityManager.createQuery("""
                            select price
                            from InstrumentPrice price
                            where price.instrument.id = :instrumentId
                            order by price.id.priceDate
                            """, InstrumentPrice.class)
                    .setParameter("instrumentId", instrumentId)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    public void saveAll(Long instrumentId, List<HistoricalPrice> prices) {
        if (prices.isEmpty()) {
            return;
        }

        EntityManager entityManager = PersistenceManager.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            Instrument instrument = entityManager.getReference(Instrument.class, instrumentId);
            for (HistoricalPrice price : prices) {
                entityManager.merge(new InstrumentPrice(
                        instrument,
                        price.priceDate(),
                        price.openPrice(),
                        price.highPrice(),
                        price.lowPrice(),
                        price.closePrice(),
                        price.adjustedClosePrice(),
                        price.volume(),
                        price.currency()));
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
