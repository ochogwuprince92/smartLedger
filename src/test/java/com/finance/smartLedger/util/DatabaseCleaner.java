package com.finance.smartLedger.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseCleaner {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/smartledger_db";
        String user = "postgres";
        String password = "ogwaa123";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Drop all tables and recreate schema
            stmt.execute("DROP SCHEMA public CASCADE");
            stmt.execute("CREATE SCHEMA public");
            stmt.execute("GRANT ALL ON SCHEMA public TO postgres");
            stmt.execute("GRANT ALL ON SCHEMA public TO public");
            
            System.out.println("Database schema cleaned successfully!");
        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
