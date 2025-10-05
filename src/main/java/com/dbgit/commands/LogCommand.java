package com.dbgit.commands;

import picocli.CommandLine.Command;
import com.dbgit.model.Config;
import com.dbgit.util.HeadUtils;

import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;

@Command(name = "log", description = "Show commit history for the active database")
public class LogCommand implements Runnable {

    @Override
    public void run() {
        try {
            Config cfg = Config.getInstance();
            String activeDb = cfg.database.name;
            Path commitsDir = Paths.get(".dbgit/commits", activeDb);

            if (!Files.exists(commitsDir)) {
                System.out.println("[!] No commits found for database: " + activeDb);
                return;
            }

            System.out.println("Commit history for database: " + activeDb + "\n");

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
                                    var yaml = new Yaml();
                                    var data = yaml.load(Files.readString(metadataFile));
                                    if (data instanceof Map<?, ?> map) {
                                        Object msgObj = map.get("message");
                                        Object tsObj = map.get("timestamp");

                                        message = msgObj != null ? msgObj.toString() : "";
                                        timestamp = tsObj != null ? tsObj.toString() : "";
                                    }
                                } catch (Exception e) {
                                    message = "[Error reading metadata]";
                                }
                            }

                            String headMarker = commitId.equals(HeadUtils.getHead()) ? " <- HEAD" : "";
                            System.out.printf("%s - %s (%s)%s%n", commitId, message, timestamp, headMarker);
                        });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to show log: " + e.getMessage(), e);
        }
    }
}
