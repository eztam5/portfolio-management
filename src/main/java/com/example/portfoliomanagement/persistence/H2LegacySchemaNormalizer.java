package com.example.portfoliomanagement.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class H2LegacySchemaNormalizer {
    private H2LegacySchemaNormalizer() {
    }

    public static void normalize(Connection connection) throws SQLException {
        normalizeInstruments(connection);
        normalizeInstrumentLists(connection);
        normalizeInstrumentListInstruments(connection);
    }

    private static void normalizeInstruments(Connection connection) throws SQLException {
        if (!tableExists(connection, "instruments") || tableExists(connection, "INSTRUMENTS")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE \"instruments\" RENAME TO INSTRUMENTS");
            renameColumn(statement, "INSTRUMENTS", "id", "ID");
            renameColumn(statement, "INSTRUMENTS", "name", "NAME");
            renameColumn(statement, "INSTRUMENTS", "symbol", "SYMBOL");
            renameColumn(statement, "INSTRUMENTS", "isin", "ISIN");
            renameColumn(statement, "INSTRUMENTS", "latest", "LATEST");
            renameColumn(statement, "INSTRUMENTS", "currency", "CURRENCY");
        }
    }

    private static void normalizeInstrumentLists(Connection connection) throws SQLException {
        if (!tableExists(connection, "instrument_lists") || tableExists(connection, "INSTRUMENT_LISTS")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE \"instrument_lists\" RENAME TO INSTRUMENT_LISTS");
            renameColumn(statement, "INSTRUMENT_LISTS", "id", "ID");
            renameColumn(statement, "INSTRUMENT_LISTS", "name", "NAME");
        }
    }

    private static void normalizeInstrumentListInstruments(Connection connection) throws SQLException {
        if (!tableExists(connection, "instrument_list_instruments")
                || tableExists(connection, "INSTRUMENT_LIST_INSTRUMENTS")) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE \"instrument_list_instruments\" RENAME TO INSTRUMENT_LIST_INSTRUMENTS");
            renameColumn(statement, "INSTRUMENT_LIST_INSTRUMENTS", "instrument_list_id", "INSTRUMENT_LIST_ID");
            renameColumn(statement, "INSTRUMENT_LIST_INSTRUMENTS", "instrument_id", "INSTRUMENT_ID");
        }
    }

    private static void renameColumn(Statement statement, String tableName, String oldColumnName, String newColumnName)
            throws SQLException {
        statement.execute("ALTER TABLE " + tableName + " ALTER COLUMN \"" + oldColumnName + "\" RENAME TO " + newColumnName);
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, "PUBLIC", tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }
}
