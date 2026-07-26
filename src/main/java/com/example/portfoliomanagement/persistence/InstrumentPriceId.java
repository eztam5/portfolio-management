package com.example.portfoliomanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class InstrumentPriceId implements Serializable {
    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    protected InstrumentPriceId() {
    }

    public InstrumentPriceId(Long instrumentId, LocalDate priceDate) {
        this.instrumentId = instrumentId;
        this.priceDate = priceDate;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InstrumentPriceId that)) {
            return false;
        }
        return Objects.equals(instrumentId, that.instrumentId)
                && Objects.equals(priceDate, that.priceDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentId, priceDate);
    }
}
