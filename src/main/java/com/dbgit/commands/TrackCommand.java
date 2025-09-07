package com.dbgit.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Command(
        name = "track",
        description = "Manage tracked tables.",
        subcommands = {TrackCommand.Add.class, TrackCommand.Remove.class, TrackCommand.List.class}
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
            tracked.addAll(tables);
            config.tracked_tables = List.copyOf(tracked);
            ConfigUtils.writeConfig(config);

            System.out.println("✅ Tracked tables: " + config.tracked_tables);
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
            config.tracked_tables = List.copyOf(tracked);
            ConfigUtils.writeConfig(config);

            System.out.println("✅ Tracked tables after removal: " + config.tracked_tables);
        }
    }

    @Command(name = "list", description = "List currently tracked tables.")
    public static class List implements Runnable {
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
