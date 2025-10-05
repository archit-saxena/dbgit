package com.dbgit.commands;

import picocli.CommandLine.Command;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.HeadUtils;

import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

@Command(name = "log", description = "Show commit history for the active database")
public class LogCommand implements Runnable {

    @Override
    public void run() {
        try {
            // Get active database from config.yaml
            String activeDb = ConfigUtils.getActiveDatabase();
            Path commitsDir = Paths.get(".dbgit/commits", activeDb);

            if (!Files.exists(commitsDir)) {
                System.out.println("[!] No commits found for database: " + activeDb);
                return;
            }

            System.out.println("Commit history for database: " + activeDb + "\n");

            // List all commit directories in descending order (latest first)
            try (Stream<Path> stream = Files.list(commitsDir)) {
                stream.filter(Files::isDirectory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(commitPath -> {
                            String folderName = commitPath.getFileName().toString();
                            String commitId = folderName.split("_")[0];

                            Path metadataFile = commitPath.resolve("commit_metadata.yaml");
                            String message = "";
                            String timestamp = "";

                            if (Files.exists(metadataFile)) {
                                try {
                                    var yaml = new org.yaml.snakeyaml.Yaml();
                                    var data = yaml.load(Files.readString(metadataFile));
                                    if (data instanceof java.util.Map<?, ?> map) {
                                        Object msgObj = map.get("message");
                                        Object tsObj = map.get("timestamp");

                                        message = msgObj != null ? msgObj.toString() : "";
                                        timestamp = tsObj != null ? tsObj.toString() : "";
                                    }

                                } catch (Exception e) {
                                    message = "[Error reading metadata]";
                                }
                            }

                            // Mark HEAD
                            String head = HeadUtils.getHead();
                            String headMarker = commitId.equals(head) ? " <- HEAD" : "";

                            System.out.printf("%s - %s (%s)%s%n", commitId, message, timestamp, headMarker);
                        });
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to show log: " + e.getMessage(), e);
        }
    }
}
