
package com.scc.dao;

import com.scc.model.User;

import java.sql.*;

public class Userdao {

    public Userdao() {

        try (Connection conn = databaseconnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);

        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());

        }


    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM Users WHERE roles = 'ADMIN'";
        String insertSql = "INSERT INTO Users(username, password, roles, status) VALUES('admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }

    }


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
                System.out.println("-> Successful Registration! Waiting for Approval!");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Failed to register user: " + e.getMessage());
        }

        return false;
    }

    public User login(String username, String password) {


        String sql = "SELECT id, username, password, roles, status FROM Users WHERE username = ? AND password = ?";

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, username);
             pstmt.setString(2, password);



             try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("-> Account is pending, waiting for Approval");
                        return null;
                    }
                    return new User(

                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            status
                    );

                } else {
                    System.out.println("-> Invalid username or password");
                }
            }

            } catch (SQLException e) {
            System.err.println("Error during login:  " + e.getMessage());
        }return null;


    }


}















