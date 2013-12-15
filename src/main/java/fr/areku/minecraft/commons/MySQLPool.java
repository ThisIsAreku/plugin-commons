package fr.areku.minecraft.commons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Copyright (C) plugin-commons - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Alexandre, 15/12/13
 */
public class MySQLPool {
    private static MySQLPool instance = null;
    private static String url;
    private static String user;
    private static String password;

    private Connection conn;

    public static void setConnectionData(String host, String db, int port, String user, String password) {
        MySQLPool.user = user;
        MySQLPool.password = password;
        MySQLPool.url = "jdbc:mysql://" + host + ":" + port + "/" + db;
    }

    public static MySQLPool getPool() throws ClassNotFoundException {
        if (instance == null)
            instance = new MySQLPool();
        return instance;
    }

    private MySQLPool() throws ClassNotFoundException {
        loadClass();
    }

    private void loadClass() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Could not create new connection", e);
        }
    }

    public void connect() throws SQLException {
        openConnection();
        if (!checkConnectionIsAlive(false)) {
            if (conn != null) conn.close();
            throw new SQLException("Could not create new connection");
        }
    }

    private void openConnection() throws SQLException {
        try {
            conn = DriverManager.getConnection(this.url, this.user, this.password);
        } catch (SQLException e) {
            throw new SQLException("Could not create new connection", e);
        }
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }

    public boolean checkConnectionIsAlive(boolean reopen) throws SQLException {
        if (conn == null) {
            if (reopen) {
                openConnection();
                return checkConnectionIsAlive(false);
            }
            return false;
        }
        if (conn.isClosed()) {
            return false;
        }
        if (conn.isValid(10)) {
            return true;
        } else {
            if (reopen) {
                openConnection();
                return checkConnectionIsAlive(false);
            }
            return false;
        }
    }

    public Statement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
