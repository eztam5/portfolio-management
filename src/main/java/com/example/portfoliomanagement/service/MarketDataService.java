package com.example.portfoliomanagement.service;

import com.example.portfoliomanagement.marketdata.HistoricalPrice;
import com.example.portfoliomanagement.marketdata.HistoricalPriceProvider;
import com.example.portfoliomanagement.marketdata.YahooFinanceHistoricalPriceProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MarketDataService {
    private final HistoricalPriceProvider historicalPriceProvider;

    public MarketDataService() {
        this(new YahooFinanceHistoricalPriceProvider());
    }

    public MarketDataService(HistoricalPriceProvider historicalPriceProvider) {
        this.historicalPriceProvider = historicalPriceProvider;
    }

    public List<HistoricalPrice> loadDailyPrices(String symbol, LocalDate from, LocalDate to) throws IOException {
        return historicalPriceProvider.loadDailyPrices(symbol, from, to);
    }
}
