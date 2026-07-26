package com.example.portfoliomanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "instruments")
public class Instrument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String symbol;

    @Column(length = 12, unique = true)
    private String isin;

    @Column(precision = 19, scale = 4)
    private BigDecimal latest;

    @Column(length = 3)
    private String currency;

    @ManyToMany(mappedBy = "instruments")
    private Set<InstrumentList> lists = new HashSet<>();

    @OneToMany(mappedBy = "instrument")
    private Set<InstrumentPrice> prices = new HashSet<>();

    protected Instrument() {
    }

    public Instrument(String name, String symbol, String isin, BigDecimal latest, String currency) {
        this.name = name;
        this.symbol = symbol;
        this.isin = isin;
        this.latest = latest;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getIsin() {
        return isin;
    }

    public BigDecimal getLatest() {
        return latest;
    }

    public String getCurrency() {
        return currency;
    }

    public Set<InstrumentPrice> getPrices() {
        return prices;
    }
}
