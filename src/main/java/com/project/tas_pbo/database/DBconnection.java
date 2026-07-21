package com.project.tas_pbo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Koneksi ke database MySQL
public class DBconnection {

    private static final String URL = "jdbc:mysql://localhost:3306/pos_db";
    private static final String USER = "root";
    private static final String PASSWORD = "zte12367c$";

    // Mendapatkan koneksi ke database
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
