package com.scc;


import com.scc.dao.DatabaseInitializer;
import com.scc.dao.Userdao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    private static final Scanner sc =  new Scanner(System.in);
    private static final Userdao userdao = new Userdao();

    public static void main(String[] args) {


        DatabaseInitializer.initialize();

         Scanner scanner =  new Scanner(System.in);
         Userdao userdao = new Userdao();
        Authentication auth = new Authentication(userdao, scanner);
        auth.start();
        scanner.close();
    }



}
