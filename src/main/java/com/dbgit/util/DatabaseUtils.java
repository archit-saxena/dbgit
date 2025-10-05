package com.dbgit.util;

import com.dbgit.model.Config;
import org.json.JSONObject;
import org.json.JSONArray;
import java.sql.*;

public class DatabaseUtils {

    public static String buildJdbcUrl(Config.Database dbConfig) {
        return String.format("jdbc:mysql://%s:%d/%s", dbConfig.host, dbConfig.port, dbConfig.name);
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

    public static boolean databaseExists(Config.Database dbConfig, String newDbName) {
        String url = buildJdbcUrl(dbConfig);

        try (Connection conn = DriverManager.getConnection(url, dbConfig.user, dbConfig.password)) {
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

    public static String dumpTableSchema(Config.Database dbConfig, String tableName) {
        String jdbcUrl = buildJdbcUrl(dbConfig);
        String schemaSql = "";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbConfig.user, dbConfig.password);
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

    public static String dumpTableData(Config.Database dbConfig, String tableName) {
        String jdbcUrl = buildJdbcUrl(dbConfig);
        JSONArray jsonArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbConfig.user, dbConfig.password);
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

    public static void restoreSchema(Config.Database db, String tableName, String sql) {
        String url = buildJdbcUrl(db);
        try (Connection conn = DriverManager.getConnection(url, db.user, db.password);
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS=0");       // Disable FKs
            stmt.execute("DROP TABLE IF EXISTS " + tableName); // Drop table if exists
            stmt.execute(sql);                              // Recreate table
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");      // Re-enable FKs

        } catch (SQLException e) {
            throw new RuntimeException("Failed to restore schema for table " + tableName + ": " + e.getMessage(), e);
        }
    }

    public static void restoreData(Config.Database db, String table, String jsonData) {
        String url = buildJdbcUrl(db);
        JSONArray rows = new JSONArray(jsonData);

        try (Connection conn = DriverManager.getConnection(url, db.user, db.password)) {
            conn.setAutoCommit(false);

            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                StringBuilder cols = new StringBuilder();
                StringBuilder vals = new StringBuilder();

                for (String key : row.keySet()) {
                    cols.append(key).append(",");
                    vals.append("?,");
                }

                String sql = "INSERT INTO " + table +
                        "(" + cols.substring(0, cols.length() - 1) + ") " +
                        "VALUES (" + vals.substring(0, vals.length() - 1) + ")";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int idx = 1;
                    for (String key : row.keySet()) {
                        pstmt.setObject(idx++, row.get(key));
                    }
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to restore data for table " + table + ": " + e.getMessage(), e);
        }
    }

}
