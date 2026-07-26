package com.example.portfoliomanagement.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cash_accounts")
public class CashAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 2000)
    private String note;

    protected CashAccount() {
    }

    public CashAccount(String name, String currency, String note) {
        this.name = name;
        this.currency = currency;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNote() {
        return note;
    }
}
