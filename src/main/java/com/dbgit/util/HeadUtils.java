package com.dbgit.util;

import org.yaml.snakeyaml.Yaml;

import java.nio.file.*;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

public class HeadUtils {

    private static final Path HEAD_FILE = Paths.get(".dbgit/HEAD");
    private static final Yaml yaml = new Yaml();

    /** Get HEAD for active database */
    public static String getHead() {
        String activeDb = ConfigUtils.getActiveDatabase();
        try {
            if (!Files.exists(HEAD_FILE)) return null;

            Map<String, String> heads = yaml.load(Files.readString(HEAD_FILE));
            if (heads == null) return null;
            return heads.get(activeDb);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read HEAD file: " + e.getMessage(), e);
        }
    }

    /** Update HEAD for active database */
    public static void updateHead(String commitId) {
        String activeDb = ConfigUtils.getActiveDatabase();
        try {
            Map<String, String> heads = new LinkedHashMap<>();
            if (Files.exists(HEAD_FILE)) {
                Map<String, String> existing = yaml.load(Files.readString(HEAD_FILE));
                if (existing != null) heads.putAll(existing);
            }

            heads.put(activeDb, commitId);
            String content = yaml.dump(heads);
            Files.writeString(HEAD_FILE, content);

        } catch (IOException e) {
            throw new RuntimeException("Failed to update HEAD file: " + e.getMessage(), e);
        }
    }
}
