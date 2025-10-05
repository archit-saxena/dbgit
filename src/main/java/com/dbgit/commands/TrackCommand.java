package com.dbgit.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.dbgit.model.Config;
import com.dbgit.util.DatabaseUtils;

import java.util.*;

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
            Config cfg = Config.getInstance();
            Set<String> tracked = new HashSet<>(cfg.tracked_tables);

            for (String table : tables) {
                if (DatabaseUtils.tableExists(table)) {
                    tracked.add(table);
                    System.out.println(";) Added: " + table);
                } else {
                    System.out.println("XD Table not found in DB, skipped: " + table);
                }
            }
            cfg.tracked_tables = new ArrayList<>(tracked);
            com.dbgit.util.ConfigUtils.writeConfig(cfg);
        }
    }

    @Command(name = "remove", description = "Remove tables from tracked list.")
    public static class Remove implements Runnable {
        @Parameters(arity = "1..*", description = "Table names to remove from tracking.")
        List<String> tables;

        @Override
        public void run() {
            Config cfg = Config.getInstance();
            cfg.tracked_tables.removeAll(tables);
            com.dbgit.util.ConfigUtils.writeConfig(cfg);
            tables.forEach(t -> System.out.println("Removed: " + t));
        }
    }

    @Command(name = "list", description = "List all tracked tables.")
    public static class ListTracked implements Runnable {
        @Override
        public void run() {
            Config cfg = Config.getInstance();
            if (cfg.tracked_tables.isEmpty()) System.out.println("No tables are currently tracked.");
            else cfg.tracked_tables.forEach(t -> System.out.println("- " + t));
        }
    }
}
