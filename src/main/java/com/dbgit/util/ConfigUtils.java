package com.dbgit.util;

import com.dbgit.model.Config;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Map;
import java.util.LinkedHashMap;

public class ConfigUtils {

    private static final Path CONFIG_PATH = Path.of(".dbgit/config.yaml");
    private static final Yaml yaml;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    public static Config readConfig() {
        try {
            String content = Files.readString(CONFIG_PATH);
            return yaml.loadAs(content, Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config.yaml", e);
        }
    }

    public static void writeConfig(Config config) {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

            Representer representer = new Representer(options);
            representer.getPropertyUtils().setSkipMissingProperties(true);

            Yaml yaml = new Yaml(representer, options);

            // Convert Config object to a plain Map<String, Object>
            Map<String, Object> data = new LinkedHashMap<>();
            Map<String, Object> db = new LinkedHashMap<>();
            db.put("url", config.database.url);
            db.put("user", config.database.user);
            db.put("password", config.database.password);

            data.put("database", db);
            data.put("tracked_tables", config.tracked_tables);

            String yamlContent = yaml.dump(data);
            Files.writeString(CONFIG_PATH, yamlContent);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write config.yaml", e);
        }
    }

}
