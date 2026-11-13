package com.scc.dao;

import com.scc.model.Item;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public boolean registerItem(String itemName, String category, String description, String location, int userId, String username) {
        String sql = "INSERT INTO items (item_name, category, description, location, reported_by) VALUES (?, ?, ?, ?, ?)";
        boolean success = false;

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, itemName);
            pstmt.setString(2, category); // e.g., "LOST" or "FOUND"
            pstmt.setString(3, description);
            pstmt.setString(4, location);
            pstmt.setInt(5, userId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                success = true;
                System.out.println("-> [SUCCESS] Item reported successfully!");
            }
        } catch (SQLException e) {
            System.err.println("-> [ERROR] Failed to register item: " + e.getMessage());
        }

        if (success) {
            // Logs the Core Business Process to meet your deliverable checklist requirement #3
            activityLogDao.logAction(userId, username, "Core Business Process",
                    "Reported " + category.toLowerCase() + " item: " + itemName + " found/lost at " + location);
            return true;
        }
        return false;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT id, item_name, category, description, location, status, reported_by, timestamp FROM items ORDER BY timestamp DESC";

        try (Connection conn = databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("status"),
                        rs.getInt("reported_by"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("-> [ERROR] Failed to fetch items: " + e.getMessage());
        }
        return items;
    }
}