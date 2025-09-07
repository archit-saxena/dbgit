package com.dbgit.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.DatabaseUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Command(
        name = "track",
        description = "Manage tracked tables.",
        subcommands = {
                TrackCommand.Add.class,
                TrackCommand.Remove.class,
                TrackCommand.ListTracked.class
        }
)
public class TrackCommand {

    @Command(name = "add", description = "Add tables to tracked list.")
    public static class Add implements Runnable {
        @Parameters(arity = "1..*", description = "Table names to track.")
        List<String> tables;

        @Override
        public void run() {
            Config config = ConfigUtils.readConfig();

            Set<String> tracked = new HashSet<>(config.tracked_tables);

            for (String table : tables) {
                if (DatabaseUtils.tableExists(
                        config.database, table)) {
                    tracked.add(table);
                    System.out.println("✅ Added: " + table);
                } else {
                    System.out.println("⚠️  Table not found in DB, skipped: " + table);
                }
            }

            config.tracked_tables = new ArrayList<>(tracked);
            ConfigUtils.writeConfig(config);

            System.out.println("📌 Final tracked tables: " + config.tracked_tables);
        }

    }

    @Command(name = "remove", description = "Remove tables from tracked list.")
    public static class Remove implements Runnable {
        @Parameters(arity = "1..*", description = "Table names to remove from tracking.")
        List<String> tables;

        @Override
        public void run() {
            Config config = ConfigUtils.readConfig();
            Set<String> tracked = new HashSet<>(config.tracked_tables);
            tracked.removeAll(tables);
            config.tracked_tables = new ArrayList<>(tracked);  // ✅ fixed here
            ConfigUtils.writeConfig(config);

            System.out.println("✅ Tracked tables after removal: " + config.tracked_tables);
        }
    }

    @Command(name = "list", description = "List currently tracked tables.")
    public static class ListTracked implements Runnable {  // ✅ renamed class
        @Override
        public void run() {
            Config config = ConfigUtils.readConfig();
            if (config.tracked_tables.isEmpty()) {
                System.out.println("ℹ️ No tables are currently tracked.");
            } else {
                System.out.println("Tracked tables:");
                config.tracked_tables.forEach(t -> System.out.println(" - " + t));
            }
        }
    }
}
