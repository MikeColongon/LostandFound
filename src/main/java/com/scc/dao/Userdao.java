
package com.scc.dao;

import com.scc.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class Userdao {
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

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

        int id = -1;
        String uname = null;
        String pwd = null;
        String role = null;
        String status = null;
        boolean found = false;

        // 1. Execute the query and close the connection first
        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("-> Account is pending, waiting for Approval");
                        return null;
                    }
                    id = rs.getInt("id");
                    uname = rs.getString("username");
                    pwd = rs.getString("password");
                    role = rs.getString("roles");
                    found = true;
                } else {
                    System.out.println("-> Invalid username or password");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login:  " + e.getMessage());
            return null;
        }

        // 2. Database connection is now closed, so writing the log won't lock the file!
        if (found) {
            activityLogDao.logAction(id, uname, "Authentication",
                    "User " + uname + " (ID: " + id + ") logged in.");
            return new User(id, uname, pwd, role, status);
        }

        return null;
    }


    public List<User> getAllUser() {
        List<User> usersList = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM Users";

        try (Connection conn = databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
        }
        return usersList;
    }



    public List<User> getPendingUser(){
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE status = 'PENDING'";

        try(Connection conn = databaseconnection.getConnection()){
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                pendingList.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: "+e.getMessage());
        }
        return pendingList;
    }



    public boolean approveUser(int userId){
        String sql = "UPDATE Users SET status = 'APPROVED' WHERE id = ?";

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();

            if(rowsUpdated > 0){
                System.out.println("-> User ID: " + userId + " approved successfully!");


                activityLogDao.logAction(userId, "Admin", "Account Management",
                        "Admin approved User [ID: " + userId + "]");

                return true;
            }else{
                System.out.println("-> ID not found.");
            }

        }catch (Exception e){
            System.err.println("Error approving user: "+e.getMessage());
        }
        return false;
    }


    public boolean updateUserAndStatus(int userId, String newRoles, String newStatus){
        String sql = "UPDATE Users SET roles = ?, status = ? WHERE id = ?";

        try(Connection conn  = databaseconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, newRoles.toUpperCase(Locale.ROOT));
            pstmt.setString(2, newStatus.toUpperCase(Locale.ROOT));
            pstmt.setInt(3, userId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0){
                System.out.println("-> User ID: " + userId + " updated successfully!");

                // Log Account Management Event (Role/Status Edit) using concatenation
                activityLogDao.logAction(userId, "Admin", "Account Management",
                        "Role changed to " + newRoles + ", Status changed to " + newStatus);

                return true;
            }else{
                System.out.println("-> User Id not found.");
                return false;
            }

        }catch (SQLException e){
            System.err.println("Error updating user: "+e.getMessage());
        }
        return false;
    }

    // Admin adding a user directly (automatically APPROVED)
    public boolean adminAddUser(String username, String password, String role) {
        String sql = "INSERT INTO Users(username, password, roles, status) VALUES(?, ?, ?, 'APPROVED')";
        boolean success = false;
        int newUserId = -1;

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase(Locale.ROOT));

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("-> User added successfully!");
                success = true;

                // Get the generated user ID for logging
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newUserId = generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to add user: " + e.getMessage());
        }

        // Log the account management action after connection is closed
        if (success) {
            activityLogDao.logAction(newUserId > 0 ? newUserId : 0, "Admin", "Account Management",
                    "Admin created new user: " + username + " with role " + role.toUpperCase());
            return true;
        }
        return false;
    }


    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE id = ?";
        boolean deleted = false;

        try (Connection conn = databaseconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("-> User ID: " + userId + " deleted successfully!");
                deleted = true;
            } else {
                System.out.println("-> User ID not found.");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }

        // Log the account management action after connection is closed
        if (deleted) {
            activityLogDao.logAction(userId, "Admin", "Account Management",
                    "Admin deleted User [ID: " + userId + "]");
            return true;
        }
        return false;
    }


}















