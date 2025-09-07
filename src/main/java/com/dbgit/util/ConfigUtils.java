package com.dbgit.util;

import com.dbgit.model.Config;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            String yamlContent = yaml.dump(config);
            Files.writeString(CONFIG_PATH, yamlContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write config.yaml", e);
        }
    }
}
