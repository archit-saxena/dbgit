package com.dbgit.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class CommitUtils {

    public static String generateNextCommitId(String dbName) {
        Path commitsDir = Paths.get(".dbgit/commits", dbName);

        try {
            int maxId = Files.exists(commitsDir) ?
                    Files.list(commitsDir)
                            .filter(Files::isDirectory)
                            .map(p -> p.getFileName().toString().split("_")[0])
                            .filter(name -> name.matches("\\d{4}"))
                            .mapToInt(Integer::parseInt)
                            .max()
                            .orElse(0)
                    : 0;

            return String.format("%04d", maxId + 1);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate commit ID: " + e.getMessage(), e);
        }
    }

    public static void saveCommitMetadata(Path commitDir, String message) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", Instant.now().toString());

            String yamlContent = new Yaml().dump(metadata);
            Files.writeString(commitDir.resolve("commit_metadata.yaml"), yamlContent);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save commit metadata: " + e.getMessage(), e);
        }
    }

    public static String sanitizeMessage(String message) {
        return message.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9-_]", "_")
                .replaceAll("_+", "_");  // Avoid multiple consecutive underscores
    }

    public static Path getCommitDir(String dbName, String commitId) {
        Path dbCommitsDir = Paths.get(".dbgit/commits", dbName);

        try {
            return Files.list(dbCommitsDir)
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(commitId + "_"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Commit not found: " + commitId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to locate commit directory: " + e.getMessage(), e);
        }
    }

}