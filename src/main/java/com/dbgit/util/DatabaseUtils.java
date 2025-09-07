package com.dbgit.util;

import com.dbgit.model.Config;
import org.json.JSONObject;
import org.json.JSONArray;
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

    public static String dumpTableSchema(String jdbcUrl, String user, String password, String tableName) {
        String schemaSql = "";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement("SHOW CREATE TABLE " + tableName);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                schemaSql = rs.getString(2);  // Second column has the full CREATE TABLE statement
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to dump schema for table: " + tableName, e);
        }

        return schemaSql;
    }

    public static String dumpTableData(String jdbcUrl, String user, String password, String tableName) {
        JSONArray jsonArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                JSONObject row = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String column = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(column, value);
                }
                jsonArray.put(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to dump data for table: " + tableName, e);
        }

        return jsonArray.toString(2);  // Pretty-print with 2-space indentation
    }
}
