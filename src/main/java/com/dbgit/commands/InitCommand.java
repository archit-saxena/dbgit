package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.dbgit.model.Config;

import java.io.IOException;
import java.nio.file.*;

@Command(
        name = "init",
        description = "Initialize a new dbgit repository in the current directory."
)
public class InitCommand implements Runnable {

    @Option(names = {"--db"}, description = "Database name to track (required).", required = true)
    private String dbName;

    @Option(names = {"--user"}, description = "Database username (default: root).")
    private String user = "root";

    @Option(names = {"--password"}, description = "Database password (default: empty).")
    private String password = "";

    @Option(names = {"--host"}, description = "MySQL host (default: localhost).")
    private String host = "localhost";

    @Option(names = {"--port"}, description = "MySQL port (default: 3306).")
    private int port = 3306;

    @Override
    public void run() {
        Path root = Paths.get(".dbgit");

        try {
            if (Files.exists(root)) {
                System.out.println(".dbgit already exists in this directory.");
                return;
            }

            Files.createDirectories(root.resolve("commits"));
            Files.createDirectories(root.resolve("schema"));
            Files.createDirectories(root.resolve("data"));
            Files.createDirectories(root.resolve("refs"));

            // Set singleton config
            Config cfg = Config.getInstance();
            cfg.database.host = host;
            cfg.database.port = port;
            cfg.database.name = dbName;
            cfg.database.user = user;
            cfg.database.password = password;
            cfg.tracked_tables.clear();

            // Write config to disk
            com.dbgit.util.ConfigUtils.writeConfig(cfg);

            System.out.println(":* Initialized dbgit repository for database: " + dbName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize dbgit repository", e);
        }
    }
}
