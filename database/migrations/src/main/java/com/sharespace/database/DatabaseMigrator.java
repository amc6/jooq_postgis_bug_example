package com.sharespace.database;


import org.flywaydb.core.Flyway;

public class DatabaseMigrator {

    private final String url, user, password;

    public DatabaseMigrator(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void migrate() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, user, password);
        flyway.setOutOfOrder(true);
        flyway.migrate();
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Database connection url is required.");
        }
        String url = args[0];
        String user = args.length < 2 ? null : args[1];
        String password = args.length < 3 ? null : args[2];
        DatabaseMigrator migrator = new DatabaseMigrator(url, user, password);
        migrator.migrate();
    }

}
