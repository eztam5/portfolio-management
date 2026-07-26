package com.example.portfoliomanagement.marketdata;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricalPrice(
        LocalDate priceDate,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal adjustedClosePrice,
        Long volume,
        String currency) {
}
