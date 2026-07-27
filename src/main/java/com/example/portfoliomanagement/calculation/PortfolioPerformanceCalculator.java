package com.example.portfoliomanagement.calculation;

import com.example.portfoliomanagement.persistence.InstrumentPrice;
import com.example.portfoliomanagement.persistence.PortfolioTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioPerformanceCalculator {
    public List<PerformancePoint> calculate(List<PortfolioTransaction> transactions, List<InstrumentPrice> prices) {
        if (transactions.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, List<PortfolioTransaction>> transactionsByDate = groupTransactionsByDate(transactions);
        Map<Long, List<InstrumentPrice>> pricesByInstrument = groupPricesByInstrument(prices);
        Map<Long, BigDecimal> latestPriceByInstrument = new HashMap<>();
        Map<Long, Integer> nextPriceIndexByInstrument = new HashMap<>();
        Map<Long, BigDecimal> holdings = new HashMap<>();

        BigDecimal cashBalance = BigDecimal.ZERO;
        LocalDate currentDate = transactions.stream()
                .map(PortfolioTransaction::getTransactionDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate today = LocalDate.now();

        List<PerformancePoint> points = new ArrayList<>();
        while (!currentDate.isAfter(today)) {
            updateLatestPrices(currentDate, pricesByInstrument, latestPriceByInstrument, nextPriceIndexByInstrument);

            for (PortfolioTransaction transaction : transactionsByDate.getOrDefault(currentDate, List.of())) {
                cashBalance = cashBalance.add(cashEffect(transaction));
                updateHoldings(holdings, transaction);
            }

            points.add(new PerformancePoint(
                    currentDate,
                    cashBalance.add(holdingsValue(holdings, latestPriceByInstrument))));
            currentDate = currentDate.plusDays(1);
        }

        return points;
    }

    private Map<LocalDate, List<PortfolioTransaction>> groupTransactionsByDate(List<PortfolioTransaction> transactions) {
        Map<LocalDate, List<PortfolioTransaction>> transactionsByDate = new HashMap<>();
        transactions.stream()
                .sorted(Comparator.comparing(PortfolioTransaction::getTransactionDate)
                        .thenComparing(PortfolioTransaction::getId))
                .forEach(transaction -> transactionsByDate
                        .computeIfAbsent(transaction.getTransactionDate(), date -> new ArrayList<>())
                        .add(transaction));
        return transactionsByDate;
    }

    private Map<Long, List<InstrumentPrice>> groupPricesByInstrument(List<InstrumentPrice> prices) {
        Map<Long, List<InstrumentPrice>> pricesByInstrument = new HashMap<>();
        prices.stream()
                .sorted(Comparator.comparing(InstrumentPrice::getPriceDate))
                .forEach(price -> pricesByInstrument
                        .computeIfAbsent(price.getInstrument().getId(), instrumentId -> new ArrayList<>())
                        .add(price));
        return pricesByInstrument;
    }

    private void updateLatestPrices(
            LocalDate currentDate,
            Map<Long, List<InstrumentPrice>> pricesByInstrument,
            Map<Long, BigDecimal> latestPriceByInstrument,
            Map<Long, Integer> nextPriceIndexByInstrument) {
        pricesByInstrument.forEach((instrumentId, instrumentPrices) -> {
            int nextPriceIndex = nextPriceIndexByInstrument.getOrDefault(instrumentId, 0);
            while (nextPriceIndex < instrumentPrices.size()
                    && !instrumentPrices.get(nextPriceIndex).getPriceDate().isAfter(currentDate)) {
                latestPriceByInstrument.put(instrumentId, instrumentPrices.get(nextPriceIndex).getClosePrice());
                nextPriceIndex++;
            }
            nextPriceIndexByInstrument.put(instrumentId, nextPriceIndex);
        });
    }

    private BigDecimal cashEffect(PortfolioTransaction transaction) {
        BigDecimal amount = zeroIfNull(transaction.getAmount());
        BigDecimal fees = zeroIfNull(transaction.getFees());
        BigDecimal taxes = zeroIfNull(transaction.getTaxes());

        return switch (transaction.getType()) {
            case DEPOSIT, SELL -> amount.subtract(fees).subtract(taxes);
            case WITHDRAWAL -> amount.negate();
            case CASH_TRANSFER -> BigDecimal.ZERO;
            case BUY -> amount.add(fees).add(taxes).negate();
            case DIVIDEND -> amount.subtract(taxes);
        };
    }

    private void updateHoldings(Map<Long, BigDecimal> holdings, PortfolioTransaction transaction) {
        if (transaction.getInstrument() == null || transaction.getShares() == null) {
            return;
        }

        Long instrumentId = transaction.getInstrument().getId();
        switch (transaction.getType()) {
            case BUY -> holdings.merge(instrumentId, transaction.getShares(), BigDecimal::add);
            case SELL -> holdings.merge(instrumentId, transaction.getShares().negate(), BigDecimal::add);
            default -> {
            }
        }
    }

    private BigDecimal holdingsValue(Map<Long, BigDecimal> holdings, Map<Long, BigDecimal> latestPriceByInstrument) {
        return holdings.entrySet().stream()
                .map(holding -> holding.getValue().multiply(
                        latestPriceByInstrument.getOrDefault(holding.getKey(), BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
