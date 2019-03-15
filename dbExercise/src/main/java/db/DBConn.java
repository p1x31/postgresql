package db;

import java.sql.*;

public class DBConn {

    private final String username = "vxm567";
    private final String password = "3AASINO4m8";
    private final String database = "vxm567";//cslibrary
    private final String URL = "jdbc:postgresql://mod-intro-databases.cs.bham.ac.uk/" + database;

    private Connection conn = null;

    public DBConn() {
        System.out.println("Connection to Database");

        try {

            //Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException ex) {
            System.out.println("Driver not found");
            System.exit(1);
        }

        System.out.println("PostgreSQL driver registered.");


        try {
            conn = DriverManager.getConnection(URL, username, password);
        } catch (SQLException ex) {
            System.out.println("Ooops, couldn't get a connection");
            System.out.println("Check that <username> & <password> are right");
            System.exit(1);
        }

        if (conn != null) {
            System.out.println("Database accessed!");
        } else {
            System.out.println("Failed to make connection");
            System.exit(1);
        }


    }

    public Connection getConn() {
        return conn;
    }
}
