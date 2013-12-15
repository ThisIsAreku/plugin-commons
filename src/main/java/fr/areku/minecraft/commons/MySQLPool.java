package fr.areku.minecraft.commons;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    protected static void setConnectionData(String url, String user, String password) {
        MySQLPool.user = user;
        MySQLPool.password = password;
        MySQLPool.url = url;
    }

    public static MySQLPool getPool() throws Exception {
        if (instance == null)
            throw new Exception("MySQL is not ready");
        return instance;
    }

    protected MySQLPool() throws ClassNotFoundException, SQLException {
        instance = this;
        loadClass();
        connect();
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
        if (checkConnectionIsAlive(false))
            return;
        try {
            conn = DriverManager.getConnection(this.url, this.user, this.password);
        } catch (SQLException e) {
            throw new SQLException("Could not create new connection", e);
        }
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }

    public boolean checkConnectionIsAlive(boolean reopen) {
        try {
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
        } catch (SQLException e) {
            return false;
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
