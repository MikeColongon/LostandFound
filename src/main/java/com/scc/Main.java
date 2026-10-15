package com.scc;


import com.scc.dao.Userdao;
import com.scc.model.User;

import java.util.Scanner;

public class Main {

    private static final Scanner sc =  new Scanner(System.in);
    private static final Userdao userdao = new Userdao();

    public static void main(String[] args) {

        System.out.println("\n------------------------------------------------------");
        System.out.println("  LOST AND FOUND RECORDING AND CLAIM TRACKING SYSTEM  ");
        System.out.println("------------------------------------------------------");
        System.out.println("1. Log in");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        String choice = sc.nextLine();

        switch(choice){
            case "1":
                break;

            case "2":
                System.out.print("Enter new username: ");
                String username = sc.nextLine();
                System.out.print("Enter password: ");
                String password = sc.nextLine();

                System.out.print("Select role [1]-Staff | [2]-Admin: ");
                int rolechoice = sc.nextInt();
                    String role;
                switch (rolechoice){

                    case 1:
                        role = "Staff";
                        break;

                    case 2:
                        role = "Admin";
                        break;

                    default:
                        role = null;
                }

                if (role != null){

                    userdao.registerUser(username,password,role);

                }else {
                    System.out.println("Invalid role selection");

                }


                break;



        }




    }

}
