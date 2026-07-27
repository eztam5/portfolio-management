package com.example.portfoliomanagement.calculation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformancePoint(LocalDate date, BigDecimal value) {
}
