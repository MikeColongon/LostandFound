package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class databaseconnection{

    private static final  String DB_URl = "jdbc:sqlite:lostandfound.db";

    public static Connection getConnection() throws SQLException{
        Connection conn = DriverManager.getConnection(DB_URl);

        try(Statement stmt = conn.createStatement()){
            stmt.execute("PRAGMA foreign_keys = ON");

        }
        return conn;

    }

}
