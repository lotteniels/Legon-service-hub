package com.campushub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Owner: Database and Data
// TODO: implement DatabaseConnection
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:database/legon_hub.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL);
    }
}

