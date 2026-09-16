package com.patrick.bankofcli;

import com.patrick.bankofcli.config.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = ConnectionManager.getConnection()) {
            System.out.println("Connected");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
