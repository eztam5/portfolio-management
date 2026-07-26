package com.example.portfoliomanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "instrument_prices")
public class InstrumentPrice {
    @EmbeddedId
    private InstrumentPriceId id;

    @MapsId("instrumentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "open_price", precision = 19, scale = 6)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 19, scale = 6)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 19, scale = 6)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 19, scale = 6)
    private BigDecimal closePrice;

    @Column(name = "adjusted_close_price", precision = 19, scale = 6)
    private BigDecimal adjustedClosePrice;

    private Long volume;

    @Column(length = 3)
    private String currency;

    protected InstrumentPrice() {
    }

    public InstrumentPrice(
            Instrument instrument,
            LocalDate priceDate,
            BigDecimal openPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal closePrice,
            BigDecimal adjustedClosePrice,
            Long volume,
            String currency) {
        this.instrument = instrument;
        this.id = new InstrumentPriceId(instrument.getId(), priceDate);
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.adjustedClosePrice = adjustedClosePrice;
        this.volume = volume;
        this.currency = currency;
    }

    public InstrumentPriceId getId() {
        return id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public LocalDate getPriceDate() {
        return id.getPriceDate();
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getAdjustedClosePrice() {
        return adjustedClosePrice;
    }

    public Long getVolume() {
        return volume;
    }

    public String getCurrency() {
        return currency;
    }
}
