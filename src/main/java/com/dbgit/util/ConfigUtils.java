package com.dbgit.util;

import com.dbgit.model.Config;
import org.yaml.snakeyaml.Yaml;
import java.nio.file.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigUtils {

    private static final Path CONFIG_PATH = Path.of(".dbgit/config.yaml");
    private static final Yaml yaml = new Yaml();

    public static Config.Database getDatabaseConfig() {
        return Config.getInstance().database;
    }

    public static List<String> getTrackedTables() {
        return Config.getInstance().tracked_tables;
    }

    public static String getActiveDatabase() {
        Config.Database db = getDatabaseConfig();
        if (db.name == null || db.name.isEmpty())
            throw new RuntimeException("Active database not set in config.yaml");
        return db.name;
    }

    public static Config readConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                return Config.createEmpty();
            }
            String content = Files.readString(CONFIG_PATH);
            return yaml.loadAs(content, Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config.yaml", e);
        }
    }

    public static void writeConfig(Config config) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            Map<String, Object> dbMap = new LinkedHashMap<>();
            dbMap.put("host", config.database.host);
            dbMap.put("port", config.database.port);
            dbMap.put("name", config.database.name);
            dbMap.put("user", config.database.user);
            dbMap.put("password", config.database.password);

            data.put("database", dbMap);
            data.put("tracked_tables", config.tracked_tables);

            Files.writeString(CONFIG_PATH, yaml.dump(data));

            Config.reload(); // sync singleton
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config.yaml", e);
        }
    }
}
