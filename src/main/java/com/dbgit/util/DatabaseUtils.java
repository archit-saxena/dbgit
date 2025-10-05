package com.dbgit.util;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.dbgit.model.Config;

public class DatabaseUtils {

    public static String buildJdbcUrl() {
        Config.Database db = ConfigUtils.getDatabaseConfig();
        return String.format("jdbc:mysql://%s:%d/%s", db.host, db.port, db.name);
    }

    public static Connection getConnection() throws SQLException {
        Config.Database db = ConfigUtils.getDatabaseConfig();
        return DriverManager.getConnection(buildJdbcUrl(), db.user, db.password);
    }

    public static boolean tableExists(String tableName) {
        Config.Database db = ConfigUtils.getDatabaseConfig();
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, db.name);
            stmt.setString(2, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public static boolean databaseExists(String dbName) {
        Config.Database db = ConfigUtils.getDatabaseConfig();
        String sql = "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dbName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public static String dumpTableSchema(String tableName) {
        String schema = "";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("SHOW CREATE TABLE " + tableName); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) schema = rs.getString(2);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to dump schema for table " + tableName, e);
        }
        return schema;
    }

    public static String dumpTableData(String tableName) {
        JSONArray array = new JSONArray();
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tableName); ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                for (int i = 1; i <= colCount; i++) {
                    obj.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                array.put(obj);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to dump data for table " + tableName, e);
        }
        return array.toString(2);
    }

    public static void restoreSchema(String table, String sql) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("DROP TABLE IF EXISTS " + table);
            stmt.execute(sql);
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to restore schema for table " + table, e);
        }
    }

    public static void restoreData(String table, String jsonData) {
        JSONArray rows = new JSONArray(jsonData);
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                StringBuilder cols = new StringBuilder();
                StringBuilder vals = new StringBuilder();
                for (String key : row.keySet()) {
                    cols.append(key).append(",");
                    vals.append("?,");
                }
                String sql = "INSERT INTO " + table + "(" + cols.substring(0, cols.length() - 1) + ") VALUES (" + vals.substring(0, vals.length() - 1) + ")";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (String key : row.keySet()) ps.setObject(idx++, row.get(key));
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to restore data for table " + table, e);
        }
    }
}
