package com.campushub.datagen;

import java.io.*;
import java.sql.*;
import java.util.*;

public class DataGenerator {

    private static final String DB_URL = "jdbc:sqlite:database/legon_hub.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("Connected to database.");

            clearTables(conn);

            seedTable(conn, "database/seed-data/locations.csv",
                    "INSERT INTO locations (locationId, name, area, type, coordinates) VALUES (?,?,?,?,?)",
                    new String[]{"INT","STR","STR","STR","STR"});

            seedTable(conn, "database/seed-data/roads.csv",
                    "INSERT INTO roads (fromLocationId, toLocationId, distance_m, travelTime_min, roadConditionWeight) VALUES (?,?,?,?,?)",
                    new String[]{"INT","INT","DOUBLE","DOUBLE","DOUBLE"});

            seedTable(conn, "database/seed-data/resources.csv",
                    "INSERT INTO resources (resourceId, type, name, homeLocationId, capacity, availabilityStatus) VALUES (?,?,?,?,?,?)",
                    new String[]{"INT","STR","STR","INT","INT","STR"});

            seedTable(conn, "database/seed-data/service_requests.csv",
                    "INSERT INTO service_requests (requestId, sourceLocationId, destinationLocationId, category, urgency, timeSubmitted, deadline, status, fineAmountGHS) VALUES (?,?,?,?,?,?,?,?,?)",
                    new String[]{"INT","INT","INT","STR","STR","STR","STR","STR","DOUBLE"});

            printCounts(conn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearTables(Connection conn) throws SQLException {
        String[] tables = {"service_requests", "roads", "resources", "locations",
                           "algorithm_runs", "audit_events"};
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS locations (locationId INTEGER PRIMARY KEY, name TEXT, area TEXT, type TEXT, coordinates TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS roads (fromLocationId INTEGER, toLocationId INTEGER, distance_m REAL, travelTime_min REAL, roadConditionWeight REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS service_requests (requestId INTEGER PRIMARY KEY, sourceLocationId INTEGER, destinationLocationId INTEGER, category TEXT, urgency TEXT, timeSubmitted TEXT, deadline TEXT, status TEXT, fineAmountGHS REAL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS resources (resourceId INTEGER PRIMARY KEY, type TEXT, name TEXT, homeLocationId INTEGER, capacity INTEGER, availabilityStatus TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS algorithm_runs (runId INTEGER PRIMARY KEY, algorithmName TEXT, inputSize INTEGER, timeNs INTEGER, memoryKb INTEGER, dateRun TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_events (eventId INTEGER PRIMARY KEY, eventType TEXT, description TEXT, timestamp TEXT)");
            for (String t : tables) {
                stmt.executeUpdate("DELETE FROM " + t);
            }
        }
        System.out.println("Tables ready and cleared.");
    }

    private static List<String[]> readCsv(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }

    private static void seedTable(Connection conn, String csvPath, String insertSql, String[] types) throws SQLException, IOException {
        List<String[]> rows = readCsv(csvPath);
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            int inserted = 0;
            for (String[] row : rows) {
                for (int i = 0; i < types.length; i++) {
                    String value = i < row.length ? row[i] : null;
                    if (value == null || value.isEmpty()) {
                        ps.setNull(i + 1, Types.NULL);
                        continue;
                    }
                    switch (types[i]) {
                        case "INT" -> ps.setInt(i + 1, Integer.parseInt(value));
                        case "DOUBLE" -> ps.setDouble(i + 1, Double.parseDouble(value));
                        default -> ps.setString(i + 1, value);
                    }
                }
                ps.addBatch();
                inserted++;
            }
            ps.executeBatch();
            System.out.println("Inserted " + inserted + " rows from " + csvPath);
        }
    }

    private static void printCounts(Connection conn) throws SQLException {
        String[] tables = {"locations", "roads", "resources", "service_requests"};
        try (Statement stmt = conn.createStatement()) {
            for (String t : tables) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + t);
                rs.next();
                System.out.println(t + ": " + rs.getInt(1) + " rows");
            }
        }
    }
}