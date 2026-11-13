package com.scc.ui;

import com.scc.dao.Userdao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {

    private final Userdao userdao;
    private final Scanner sc;

    public Authentication(Userdao userdao, Scanner sc){
        this.userdao = userdao;
        this.sc = sc;
    }

    public void start(){
        boolean running = true;

        while (running) {
            System.out.println("\n------------------------------------------------------");
            System.out.println("  LOST AND FOUND RECORDING AND CLAIM TRACKING SYSTEM  ");
            System.out.println("------------------------------------------------------");
            System.out.println("1. Log in");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine().trim();

            switch(choice){
                case "1":
                    handleLogin();
                    break;

                case "2":
                    System.out.print("Enter new username: ");
                    String username = sc.nextLine().trim();
                    System.out.print("Enter password: ");
                    String password = sc.nextLine().trim();

                    System.out.print("Select role [1]-Staff | [2]-Admin: ");
                    String roleInput = sc.nextLine().trim();
                    String role = null;

                    try {
                        int rolechoice = Integer.parseInt(roleInput);
                        switch (rolechoice) {
                            case 1 -> role = "Staff";
                            case 2 -> role = "Admin";
                        }
                    } catch (NumberFormatException e) {
                        role = null;
                    }

                    if (role != null) {
                        userdao.registerUser(username, password, role);
                    } else {
                        System.out.println("-> [ERROR] Invalid role selection.");
                    }
                    break;

                case "3":
                    running = false;
                    System.out.println("-> Exiting application. Thank you!");
                    break;

                default:
                    System.out.println("-> [ERROR] Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    private void handleLogin(){
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = sc.nextLine().trim();

        User user = userdao.login(username, password);

        if(user != null){
            System.out.println("\n-> Welcome " + user.username() + " | ROLE: " + user.roles());

            switch (user.roles().toUpperCase()) {
                case "ADMIN" -> new Adminmenu(userdao, sc).display(user);
                case "STAFF" -> new StaffMenu(sc).display(user);
                default -> System.out.println("-> [ERROR] Account roles not found!");
            }
        }
    }
}