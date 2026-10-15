
package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Userdao {

    public boolean registerUser(String username, String password, String role) {

        String sql = "INSERT INTO Users(username, password, roles, status) "
                + "VALUES(?, ?, ?, 'PENDING')";

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println(
                        "-> Successful Registration! Waiting for Approval!"
                );
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
        }

        return false;
    }
}
