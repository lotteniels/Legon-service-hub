package com.campushub.db;

import java.sql.*;  

// Owner: Database and Data
// TODO: implement DatabaseConnection
public class DatabaseConnection { 
    private static final String URL = "jdbc:sqlite:database/legon_hub.db";

    public static Connection getConnection(){
        Connection conn = null;

        try{
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e){ 
            System.out.println("Failed to connect to SQLite: " + e.getMessage());     
        } 
        return conn;
    }
}
