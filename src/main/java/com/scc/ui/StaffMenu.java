package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.ItemDao;
import com.scc.model.Item;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class StaffMenu {
    private final ItemDao itemDao = new ItemDao();
    private final Scanner scanner;
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public StaffMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void display(User staff) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--------------------------------------");
            System.out.println("  STAFF DASHBOARD (" + staff.username() + ") ");
            System.out.println("--------------------------------------");
            System.out.println("1. Report Lost / Found Item");
            System.out.println("2. View All Reported Items");
            System.out.println("3. Logout");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleReportItem(staff);
                case "2" -> handleViewItems();
                case "3" -> {
                    loggedIn = false;
                    activityLogDao.logAction(
                            staff.id(),
                            staff.username(),
                            "Authentication",
                            "User " + staff.username() + " (ID: " + staff.id() + ") logged out."
                    );
                    System.out.println("-> Logging out...");
                }
                default -> System.out.println("-> [ERROR] Invalid option.");
            }
        }
    }

    private void handleReportItem(User staff) {
        System.out.println("\n--- REPORT LOST / FOUND ITEM ---");
        System.out.print("Enter Item Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Category (LOST / FOUND): ");
        String category = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter Location: ");
        String location = scanner.nextLine().trim();

        if (!category.equals("LOST") && !category.equals("FOUND")) {
            System.out.println("-> [ERROR] Invalid category. Must be LOST or FOUND.");
            return;
        }

        itemDao.registerItem(name, category, description, location, staff.id(), staff.username());
    }

    private void handleViewItems() {
        List<Item> items = itemDao.getAllItems();
        System.out.println("\n--- REPORTED LOST & FOUND ITEMS ---");
        if (items.isEmpty()) {
            System.out.println("-> No items reported yet.");
            return;
        }

        System.out.println("+-----+------------------+----------+----------------------+------------------+------------+");
        System.out.printf("| %-3s | %-16s | %-8s | %-20s | %-16s | %-10s |\n", "ID", "Item Name", "Category", "Description", "Location", "Status");
        System.out.println("+-----+------------------+----------+----------------------+------------------+------------+");
        for (Item item : items) {
            System.out.printf("| %-3s | %-16s | %-8s | %-20s | %-16s | %-10s |\n",
                    item.id(), item.itemName(), item.category(), item.description(), item.location(), item.status());
        }
        System.out.println("+-----+------------------+----------+----------------------+------------------+------------+");
    }
}