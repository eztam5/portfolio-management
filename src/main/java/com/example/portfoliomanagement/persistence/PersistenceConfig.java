package com.example.portfoliomanagement.persistence;

public final class PersistenceConfig {
    public static final String DATABASE_URL = "jdbc:h2:./data/portfolio-management";
    public static final String DATABASE_USER = "sa";
    public static final String DATABASE_PASSWORD = "";
    public static final String PERSISTENCE_UNIT = "portfolio-management";

    private PersistenceConfig() {
    }
}
