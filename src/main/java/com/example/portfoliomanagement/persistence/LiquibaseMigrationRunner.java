package com.example.portfoliomanagement.persistence;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public final class LiquibaseMigrationRunner {
    private static final String CHANGELOG = "db/changelog/db.changelog-master.xml";

    private LiquibaseMigrationRunner() {
    }

    public static void runMigrations() {
        try {
            Files.createDirectories(Path.of("data"));
            Class.forName("org.h2.Driver");

            try (Connection connection = DriverManager.getConnection(
                    PersistenceConfig.DATABASE_URL,
                    PersistenceConfig.DATABASE_USER,
                    PersistenceConfig.DATABASE_PASSWORD)) {
                Database database = DatabaseFactory.getInstance()
                        .findCorrectDatabaseImplementation(new JdbcConnection(connection));
                try (DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(resolveResourceRoot())) {
                    Liquibase liquibase = new Liquibase(CHANGELOG, resourceAccessor, database);
                    liquibase.update();
                }
                H2LegacySchemaNormalizer.normalize(connection);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to run database migrations.", exception);
        }
    }

    private static Path resolveResourceRoot() throws Exception {
        URI codeSource = LiquibaseMigrationRunner.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI();
        Path compiledResourceRoot = Path.of(codeSource);
        if (Files.exists(compiledResourceRoot.resolve(CHANGELOG))) {
            return compiledResourceRoot;
        }

        Path sourceResourceRoot = Path.of("src/main/resources");
        if (Files.exists(sourceResourceRoot.resolve(CHANGELOG))) {
            return sourceResourceRoot;
        }

        throw new IllegalStateException("Could not find Liquibase changelog " + CHANGELOG);
    }
}
