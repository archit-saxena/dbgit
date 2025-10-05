package com.dbgit.util;

import org.yaml.snakeyaml.Yaml;
import java.nio.file.*;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CommitUtils {

    public static String generateNextCommitId() {
        String db = ConfigUtils.getActiveDatabase();
        Path commitsDir = Paths.get(".dbgit/commits", db);
        try {
            int max = Files.exists(commitsDir) ?
                    Files.list(commitsDir).filter(Files::isDirectory)
                            .map(p -> p.getFileName().toString().split("_")[0])
                            .filter(s -> s.matches("\\d{4}")).mapToInt(Integer::parseInt).max().orElse(0)
                    : 0;
            return String.format("%04d", max + 1);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate commit ID", e);
        }
    }

    public static void saveCommitMetadata(Path commitDir, String message) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", Instant.now().toString());
            Files.writeString(commitDir.resolve("commit_metadata.yaml"), new Yaml().dump(metadata));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save commit metadata", e);
        }
    }

    public static String sanitizeMessage(String msg) {
        return msg.trim().toLowerCase().replaceAll("[^a-z0-9-_]", "_").replaceAll("_+", "_");
    }

    public static Path getCommitDir(String commitId) {
        String db = ConfigUtils.getActiveDatabase();
        Path commitsDir = Paths.get(".dbgit/commits", db);
        try {
            return Files.list(commitsDir).filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith(commitId + "_"))
                    .findFirst().orElseThrow(() -> new RuntimeException("Commit not found: " + commitId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get commit directory", e);
        }
    }
}
