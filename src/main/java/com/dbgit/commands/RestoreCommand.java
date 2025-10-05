package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.CommitUtils;
import com.dbgit.util.DatabaseUtils;
import com.dbgit.util.HeadUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

@Command(name = "restore", description = "Restore database to a previous commit snapshot")
public class RestoreCommand implements Runnable {

    @Parameters(index = "0", description = "Commit ID to revert to (e.g. 0003)")
    String commitId;

    @Option(names = {"-f", "--force"}, description = "Skip confirmation prompt")
    boolean force;

    @Override
    public void run() {
        try {
            Config config = ConfigUtils.readConfig();
            Path commitDir = CommitUtils.getCommitDir(commitId);
            Path schemaDir = commitDir.resolve("schema");
            Path dataDir = commitDir.resolve("data");
            List<String> trackedTables = config.tracked_tables;

            if (trackedTables.isEmpty()) {
                System.out.println("[!] No tables are tracked. Nothing to revert.");
                return;
            }

            if (!force) {
                System.out.println("⚠ You are about to revert the following tables to commit " + commitId + ":");
                trackedTables.forEach(t -> System.out.println(" - " + t));
                System.out.print("Proceed? (yes/no): ");
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine().trim().toLowerCase();
                if (!input.equals("yes")) {
                    System.out.println("[X] Restore aborted.");
                    return;
                }
            }

            for (String table : trackedTables) {
                Path schemaFile = schemaDir.resolve(table + ".sql");
                Path dataFile = dataDir.resolve(table + ".json");

                if (!Files.exists(schemaFile) || !Files.exists(dataFile)) {
                    System.out.println("[!] Skipped missing snapshot for table: " + table);
                    continue;
                }

                String schemaSql = Files.readString(schemaFile);
                String dataJson = Files.readString(dataFile);

                System.out.println("Restoring table: " + table);

                DatabaseUtils.restoreSchema(table, schemaSql);
                DatabaseUtils.restoreData(table, dataJson);
            }

            System.out.println("Database successfully reverted to commit " + commitId);
            HeadUtils.updateHead(commitId);
            System.out.println("Database successfully reverted to commit " + commitId
                    + " (HEAD updated for " + ConfigUtils.getActiveDatabase() + ")");

        } catch (Exception e) {
            throw new RuntimeException("Restore failed: " + e.getMessage(), e);
        }
    }
}
