package com.byteryse.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseController {
    private final Connection connection;
    private final DBConfig db;

    public DatabaseController() {
        this.db = new DBConfig();
        this.connection = connect();
    }

    private Connection connect() {
        try {
            return DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            return null;
        }
    }

    public ResultSet executeSQL(String sql, String... params) {
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            int i = 1;
            for (String param : params) {
                try {
                    int intParam = Integer.parseInt(param);
                    statement.setInt(i, intParam);
                } catch (NumberFormatException e) {
                    statement.setString(i, param);
                }
                i++;
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            return null;
        }

    }
}
