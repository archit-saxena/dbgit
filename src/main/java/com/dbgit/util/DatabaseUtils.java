package com.dbgit.util;

import com.dbgit.model.Config;
import java.sql.*;

public class DatabaseUtils {

    public static String buildJdbcUrl(Config.Database db) {
        return String.format("jdbc:mysql://%s:%d/%s", db.host, db.port, db.name);
    }

    public static boolean tableExists(Config.Database dbConfig, String tableName) {
        String jdbcUrl = buildJdbcUrl(dbConfig);
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbConfig.user, dbConfig.password)) {
            String dbName = dbConfig.name;
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

    public static boolean databaseExists(Config.Database db, String newDbName) {
        String url = buildJdbcUrl(db);

        try (Connection conn = DriverManager.getConnection(url, db.user, db.password)) {
            String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newDbName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
        return false;
    }
}
