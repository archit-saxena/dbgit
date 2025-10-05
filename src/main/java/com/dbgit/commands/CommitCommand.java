package com.dbgit.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.DatabaseUtils;
import com.dbgit.util.CommitUtils;
import com.dbgit.util.HeadUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.IOException;


@Command(name = "commit", description = "Snapshot schema and data of tracked tables")
public class CommitCommand implements Runnable {

    @Option(names = {"-m", "--message"}, description = "Commit message", required = true)
    String message;

    @Override
    public void run() {
        try {
            Config config = ConfigUtils.readConfig();

            List<String> trackedTables = config.tracked_tables;
            String commitId = CommitUtils.generateNextCommitId();
            Path commitDir = Paths.get(".dbgit/commits", config.database.name, commitId + "_" + CommitUtils.sanitizeMessage(message));
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
            System.out.println("Commit created: " + commitId);
            HeadUtils.updateHead(commitId);
            System.out.println("Commit created: " + commitId + " (HEAD updated for "
                    + ConfigUtils.getActiveDatabase() + ")");

        } catch (Exception e) {
            throw new RuntimeException("Commit failed: " + e.getMessage(), e);
        }
    }
}
