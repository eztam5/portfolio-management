package com.example.portfoliomanagement.marketdata;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface HistoricalPriceProvider {
    List<HistoricalPrice> loadDailyPrices(String symbol, LocalDate from, LocalDate to) throws IOException;
}
