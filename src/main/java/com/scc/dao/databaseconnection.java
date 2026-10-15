package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnection{

    private static final  String DB_URl = "jdbc:sqlite:lostandfound.db";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(DB_URl);
    }

}
