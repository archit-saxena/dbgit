package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.CommitUtils;
import com.dbgit.util.DatabaseUtils;
import com.dbgit.util.HeadUtils;

import java.nio.file.*;
import java.util.List;

@Command(name = "commit", description = "Snapshot schema and data of tracked tables")
public class CommitCommand implements Runnable {

    @Option(names = {"-m", "--message"}, description = "Commit message", required = true)
    String message;

    @Override
    public void run() {
        try {
            Config cfg = Config.getInstance();
            List<String> trackedTables = cfg.tracked_tables;

            if (trackedTables.isEmpty()) {
                System.out.println("[!] No tables are tracked. Nothing to commit.");
                return;
            }

            String commitId = CommitUtils.generateNextCommitId();
            Path commitDir = Paths.get(".dbgit/commits", cfg.database.name,
                    commitId + "_" + CommitUtils.sanitizeMessage(message));
            Path schemaDir = commitDir.resolve("schema");
            Path dataDir = commitDir.resolve("data");

            Files.createDirectories(schemaDir);
            Files.createDirectories(dataDir);

            for (String table : trackedTables) {
                String schemaSql = DatabaseUtils.dumpTableSchema(table);
                String dataJson = DatabaseUtils.dumpTableData(table);

                Files.writeString(schemaDir.resolve(table + ".sql"), schemaSql);
                Files.writeString(dataDir.resolve(table + ".json"), dataJson);
            }

            CommitUtils.saveCommitMetadata(commitDir, message);
            HeadUtils.updateHead(commitId);

            System.out.println("Commit created: " + commitId + " (HEAD updated for " + cfg.database.name + ")");
        } catch (Exception e) {
            throw new RuntimeException("Commit failed: " + e.getMessage(), e);
        }
    }
}
