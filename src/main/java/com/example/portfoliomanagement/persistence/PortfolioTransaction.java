package com.example.portfoliomanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class PortfolioTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionType type;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_account_id")
    private CashAccount cashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_cash_account_id")
    private CashAccount targetCashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_account_id")
    private SecurityAccount securityAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Column(precision = 19, scale = 8)
    private BigDecimal shares;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "gross_amount", precision = 19, scale = 4)
    private BigDecimal grossAmount;

    @Column(name = "gross_amount_currency", length = 3)
    private String grossAmountCurrency;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(precision = 19, scale = 4)
    private BigDecimal fees;

    @Column(precision = 19, scale = 4)
    private BigDecimal taxes;

    @Column(length = 2000)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PortfolioTransaction() {
    }

    public PortfolioTransaction(
            TransactionType type,
            LocalDate transactionDate,
            CashAccount cashAccount,
            CashAccount targetCashAccount,
            SecurityAccount securityAccount,
            Instrument instrument,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal grossAmount,
            String grossAmountCurrency,
            BigDecimal exchangeRate,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        this.type = type;
        this.transactionDate = transactionDate;
        this.cashAccount = cashAccount;
        this.targetCashAccount = targetCashAccount;
        this.securityAccount = securityAccount;
        this.instrument = instrument;
        this.shares = shares;
        this.amount = amount;
        this.currency = currency;
        this.grossAmount = grossAmount;
        this.grossAmountCurrency = grossAmountCurrency;
        this.exchangeRate = exchangeRate;
        this.fees = fees;
        this.taxes = taxes;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public CashAccount getTargetCashAccount() {
        return targetCashAccount;
    }

    public SecurityAccount getSecurityAccount() {
        return securityAccount;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getShares() {
        return shares;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public String getNote() {
        return note;
    }

    public void update(
            TransactionType type,
            LocalDate transactionDate,
            CashAccount cashAccount,
            CashAccount targetCashAccount,
            SecurityAccount securityAccount,
            Instrument instrument,
            BigDecimal shares,
            BigDecimal amount,
            String currency,
            BigDecimal grossAmount,
            String grossAmountCurrency,
            BigDecimal exchangeRate,
            BigDecimal fees,
            BigDecimal taxes,
            String note) {
        this.type = type;
        this.transactionDate = transactionDate;
        this.cashAccount = cashAccount;
        this.targetCashAccount = targetCashAccount;
        this.securityAccount = securityAccount;
        this.instrument = instrument;
        this.shares = shares;
        this.amount = amount;
        this.currency = currency;
        this.grossAmount = grossAmount;
        this.grossAmountCurrency = grossAmountCurrency;
        this.exchangeRate = exchangeRate;
        this.fees = fees;
        this.taxes = taxes;
        this.note = note;
    }
}
