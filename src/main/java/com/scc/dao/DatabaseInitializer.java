package com.scc.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void initialize() {
        new Userdao();

        String createTableSql = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "username TEXT NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE"
                + ");";

        try (Connection conn = databaseconnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            System.out.println("-> Database tables verified (including activity_logs).");
        } catch (SQLException e) {
            System.err.println("-> [ERROR] Failed to initialize activity_logs table: " + e.getMessage());
        }
    }
}