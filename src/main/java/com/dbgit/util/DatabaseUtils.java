package com.dbgit.util;

import java.sql.*;

public class DatabaseUtils {

    public static boolean tableExists(String jdbcUrl, String user, String password, String tableName) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            String dbName = extractDbName(jdbcUrl);
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = ? AND table_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, dbName);
                stmt.setString(2, tableName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
        return false;
    }

    private static String extractDbName(String jdbcUrl) {
        int lastSlash = jdbcUrl.lastIndexOf("/");
        if (lastSlash == -1) return "";
        String afterSlash = jdbcUrl.substring(lastSlash + 1);
        int question = afterSlash.indexOf("?");
        return (question == -1) ? afterSlash : afterSlash.substring(0, question);
    }
}
