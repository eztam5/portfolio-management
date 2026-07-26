package com.example.portfoliomanagement.marketdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class YahooFinanceHistoricalPriceProvider implements HistoricalPriceProvider {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<HistoricalPrice> loadDailyPrices(String symbol, LocalDate from, LocalDate to) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(chartUri(symbol, from, to))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Yahoo Finance returned HTTP " + response.statusCode()
                        + " for symbol " + symbol + ": " + responseSnippet(response.body()));
            }
            return parsePrices(symbol, response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while loading historical prices for " + symbol, exception);
        }
    }

    private URI chartUri(String symbol, LocalDate from, LocalDate to) {
        long period1 = from.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long period2 = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create("https://query1.finance.yahoo.com/v8/finance/chart/"
                + encodedSymbol
                + "?period1="
                + period1
                + "&period2="
                + period2
                + "&interval=1d&events=history&includeAdjustedClose=true");
    }

    private List<HistoricalPrice> parsePrices(String symbol, String responseBody) throws IOException {
        JsonNode chart = OBJECT_MAPPER.readTree(responseBody).path("chart");
        JsonNode error = chart.path("error");
        if (!error.isMissingNode() && !error.isNull()) {
            throw new IOException("Yahoo Finance returned an error for symbol "
                    + symbol
                    + ": "
                    + error.path("description").asText(error.toString()));
        }

        JsonNode result = chart.path("result").path(0);
        if (result.isMissingNode()) {
            throw new IOException("Yahoo Finance did not return chart data for symbol " + symbol);
        }

        String currency = nullToEmpty(result.path("meta").path("currency").asText(null));
        JsonNode timestamps = result.path("timestamp");
        JsonNode quote = result.path("indicators").path("quote").path(0);
        JsonNode adjustedClose = result.path("indicators").path("adjclose").path(0).path("adjclose");

        List<HistoricalPrice> prices = new ArrayList<>();
        for (int index = 0; index < timestamps.size(); index++) {
            BigDecimal closePrice = decimalAt(quote.path("close"), index);
            if (closePrice == null) {
                continue;
            }

            LocalDate priceDate = LocalDate.ofEpochDay(timestamps.path(index).asLong() / 86_400);
            prices.add(new HistoricalPrice(
                    priceDate,
                    decimalAt(quote.path("open"), index),
                    decimalAt(quote.path("high"), index),
                    decimalAt(quote.path("low"), index),
                    closePrice,
                    decimalAt(adjustedClose, index),
                    longAt(quote.path("volume"), index),
                    currency));
        }
        return prices;
    }

    private BigDecimal decimalAt(JsonNode values, int index) {
        JsonNode value = values.path(index);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return new BigDecimal(value.asText());
    }

    private Long longAt(JsonNode values, int index) {
        JsonNode value = values.path(index);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return value.asLong();
    }

    private String nullToEmpty(String value) {
        return value == null ? null : value;
    }

    private String responseSnippet(String responseBody) {
        if (responseBody == null || responseBody.length() <= 250) {
            return responseBody;
        }
        return responseBody.substring(0, 250);
    }
}
