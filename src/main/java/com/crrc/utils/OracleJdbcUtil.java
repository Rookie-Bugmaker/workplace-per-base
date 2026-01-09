package com.crrc.utils;
import oracle.jdbc.OracleDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class OracleJdbcUtil {
    private static final String URL = "jdbc:oracle:thin:@172.22.112.57:1521/ORCL";
    private static final String USERNAME = "hcmdbcenter";
    private static final String PASSWORD = "Crchcm_2022";

    static {
        try {
            // 注册Oracle驱动
            OracleDriver oracleDriver = new oracle.jdbc.OracleDriver();
            DriverManager.registerDriver(oracleDriver);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register Oracle JDBC driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        // 可添加其他连接参数
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        props.setProperty("oracle.jdbc.ReadTimeout", "30000");
        return DriverManager.getConnection(URL, props);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing Oracle connection: " + e.getMessage());
            }
        }
    }
}