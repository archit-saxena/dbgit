package com.dbgit.util;

import org.yaml.snakeyaml.Yaml;
import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

public class HeadUtils {

    private static final Path HEAD_FILE = Paths.get(".dbgit/HEAD");
    private static final Yaml yaml = new Yaml();

    public static String getHead() {
        String db = ConfigUtils.getActiveDatabase();
        try {
            if (!Files.exists(HEAD_FILE)) return null;
            Map<String, String> heads = yaml.load(Files.readString(HEAD_FILE));
            return heads != null ? heads.get(db) : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read HEAD file", e);
        }
    }

    public static void updateHead(String commitId) {
        String db = ConfigUtils.getActiveDatabase();
        try {
            Map<String, String> heads = new LinkedHashMap<>();
            if (Files.exists(HEAD_FILE)) {
                Map<String, String> existing = yaml.load(Files.readString(HEAD_FILE));
                if (existing != null) heads.putAll(existing);
            }
            heads.put(db, commitId);
            Files.writeString(HEAD_FILE, yaml.dump(heads));
        } catch (IOException e) {
            throw new RuntimeException("Failed to update HEAD file", e);
        }
    }
}
