package com.example.filmy;

import java.sql.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:filmy.db";

    public static void initialize() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY," +
                    "email TEXT UNIQUE," +
                    "password TEXT);");

            stmt.execute("CREATE TABLE IF NOT EXISTS films (" +
                    "id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "genre TEXT," +
                    "actors TEXT," +
                    "watched INTEGER DEFAULT 0," +
                    "rating INTEGER DEFAULT 0);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}