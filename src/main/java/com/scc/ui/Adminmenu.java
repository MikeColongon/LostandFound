package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.Userdao;
import com.scc.model.ActivityLog;
import com.scc.model.User;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Adminmenu {

    private final Userdao userDao;
    private final Scanner scanner;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public Adminmenu(Userdao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void display(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--------------------------------------");
            System.out.println("  ADMIN DASHBOARD  ");
            System.out.println("--------------------------------------");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. View System Adudit logs ");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUser();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> handleAddUser();
                case "5" -> updateUser(admin);
                case "6" -> handleDeleteUser();
                case "7" -> viewSystemAuditLogs();
                case "8" -> {
                    loggedIn = false;

                    activityLogDao.logAction(
                            admin.id(),
                            admin.username(),
                            "Authentication",
                            "User " + admin.username() + " (ID: " + admin.id() + ") logged out."
                    );

                    System.out.println("-> Logging out...");
                }
                default -> System.out.println("-> [ERROR] Invalid option.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("-> No Records Found");
            return;
        }

        System.out.println("+-----+-----------------+------------+------------+");
        System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n", "ID", "Username", "Role", "Status");
        System.out.println("+-----+-----------------+------------+------------+");
        for (User u : users) {
            System.out.printf("| %-3s | %-15s | %-10s | %-10s |\n",
                    u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+-----+-----------------+------------+------------+");
    }

    private void viewAllUser() {
        displayUserTable(userDao.getAllUser(), "ALL REGISTERED USERS");
    }

    private void viewPendingUsers() {
        displayUserTable(userDao.getPendingUser(), "PENDING APPROVALS");
    }

    private void approveUser() {
        List<User> pendingUsers = userDao.getPendingUser();
        displayUserTable(pendingUsers, "PENDING APPROVALS");

        if (pendingUsers.isEmpty()) {
            return;
        }

        System.out.print("Enter User ID to approve (or 0 to cancel): ");
        int userId;
        try {
            userId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("-> [ERROR] Please enter a valid number.");
            return;
        }

        if (userId == 0) {
            System.out.println("-> Operation cancelled by the user.");
            return;
        }

        boolean exists = pendingUsers.stream().anyMatch(u -> u.id() == userId);
        if (!exists) {
            System.out.println("-> [ERROR] User ID not found in pending list.");
            return;
        }

        userDao.approveUser(userId);
        System.out.println("-> [SUCCESS] User successfully approved.");


    }

    private void updateUser(User currentAdmin){
        List<User> users = userDao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if(users.isEmpty()){
            return;
        }

        int userId = readIntInput("Enter selected ID to update (or 0 to cancel): ");

        if (userId == 0){
            System.out.println("-> Operation Cancelled");
            return;
        }
        if(userId == currentAdmin.id()){
            System.out.println(" -> [WARNING] Modifying current active Admin is restricted");
        }

        String newRole = readValidRole();
        if(newRole ==null){
            System.out.println("-> Operation Cancelled");
            return;
        }

        String newStatus = readValidStatus();

        if(newStatus ==null){
            System.out.println("-> Operation Cancelled");
            return;
        }

        userDao.updateUserAndStatus(userId, newRole, newStatus);

    }

    private String readValidRole(){
        while(true){
            System.out.print("Enter Role (CLAIMANT/STAFF/ADMIN) [0 to cancel]: ");
            String role  = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
            if (role.equals("0") || role.equals("CANCEL")){
                return null;
            }

            if(role.equals("CLAIMANT") || role.equals("STAFF") || role.equals("ADMIN")){
                return role;
            }
            System.out.println(" -> [ERROR] Invalid Role.");
        }

    }

    private String readValidStatus(){
        while(true){
            System.out.print("Enter Status (APPROVED / PENDING) [0 to cancel]: ");
            String status  = scanner.nextLine().trim().toUpperCase(Locale.ROOT);
            if (status.equals("0") || status.equals("CANCEL")){
                return null;
            }

            if(status.equals("APPROVED") || status.equals("PENDING")){
                return status;
            }
            System.out.println(" -> [ERROR] Invalid status. Must be APPROVED or PENDING");
        }

    }

    private int readIntInput(String prompt){
        while(true){
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            }catch (NumberFormatException e){
                System.out.println("-> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    private void displayLogsTable(List<ActivityLog> logs, String title) {
        System.out.println("\n--- " + title + " ---");
        if (logs.isEmpty()) {
            System.out.println("-> No Audit Logs Found.");
            return;
        }

        System.out.println("+-------+----------+-----------------+--------------------+----------------------+");
        String header = String.format("| %-5s | %-8s | %-15s | %-18s | %-20s |", "ID", "User ID", "Username", "Action Type", "Timestamp");
        System.out.println(header);
        System.out.println("+-------+----------+-----------------+--------------------+----------------------+");

        for (ActivityLog log : logs) {
            String row = String.format("| %-5d | %-8d | %-15s | %-18s | %-20s |",
                    log.id(), log.userId(), log.username(), log.actionType(), log.timestamp());
            System.out.println(row);
        }
        System.out.println("+-------+----------+-----------------+--------------------+----------------------+");
    }

    private void viewSystemAuditLogs() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----------------------------------------");
            System.out.println("SYSTEM AUDIT LOGS");
            System.out.println("----------------------------------------");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Menu");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    List<ActivityLog> logs = activityLogDao.getAllLogs();
                    printLogsTable(logs);
                }
                case "2" -> {
                    System.out.print("Enter User ID to filter: ");
                    try {
                        int userId = Integer.parseInt(scanner.nextLine());
                        List<ActivityLog> logs = activityLogDao.getLogsByUserId(userId);
                        printLogsTable(logs);
                    } catch (NumberFormatException e) {
                        System.out.println("-> [ERROR] Invalid User ID format. Please enter a number.");
                    }
                }
                case "3" -> back = true;
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
    }

    private void printLogsTable(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("-> No activity logs found.");
            return;
        }

        // Print aligned table header matching instructor spec: %-5s | %-8s | %-18s | %-35s | %-20s
        System.out.println("\n------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-8s | %-18s | %-55s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");

        for (ActivityLog log : logs) {
            System.out.printf("%-5d | %-8d | %-18s | %-55s | %-20s%n",
                    log.id(),
                    log.userId(),
                    log.actionType(),
                    log.description(),
                    log.timestamp()
            );
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
    }

    private void handleAddUser() {
        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter password: ");
        String newPassword = scanner.nextLine();
        System.out.print("Enter role (STAFF / ADMIN): ");
        String newRole = scanner.nextLine();

        switch (newRole){

            case "STAFF":
                newRole = "Staff";
                break;

            case "ADMIN":
                newRole = "Admin";
                break;

            default:
                newRole = null;
        }

        if (newRole != null){

            userDao.adminAddUser(newUsername, newPassword, newRole);

        }else {
            System.out.println("Invalid role selection");

        }


    }

    private void handleDeleteUser() {
        System.out.print("Enter User ID to delete: ");
        try {
            int deleteId = Integer.parseInt(scanner.nextLine());
            userDao.deleteUser(deleteId);
        } catch (NumberFormatException e) {
            System.out.println("-> [ERROR] Invalid User ID format. Please enter a number.");
        }
    }

}