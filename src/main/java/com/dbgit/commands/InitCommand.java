package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.*;

@Command(
        name = "init",
        description = "Initialize a new dbgit repository in the current directory."
)
public class InitCommand implements Runnable {

    @Option(
            names = {"--db"},
            description = "Database name to track (required).",
            required = true
    )
    private String dbName;

    @Option(
            names = {"--user"},
            description = "Database username (default: root)."
    )
    private String user = "root";

    @Option(
            names = {"--password"},
            description = "Database password (default: empty)."
    )
    private String password = "";

    @Option(
            names = {"--host"},
            description = "MySQL host (default: localhost)."
    )
    private String host = "localhost";

    @Option(
            names = {"--port"},
            description = "MySQL port (default: 3306)."
    )
    private int port = 3306;

    @Override
    public void run() {
        Path root = Paths.get(".dbgit");

        try {
            if (Files.exists(root)) {
                System.out.println("⚠️  .dbgit already exists in this directory.");
                return;
            }

            // create directories
            Files.createDirectories(root.resolve("commits"));
            Files.createDirectories(root.resolve("schema"));
            Files.createDirectories(root.resolve("data"));
            Files.createDirectories(root.resolve("refs"));

            // build JDBC URL
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);

            // config.yaml content
            String configContent = String.format("""
                database:
                  url: %s
                  user: %s
                  password: %s

                tracked_tables: []
                """, jdbcUrl, user, password);

            // write config.yaml
            Path configFile = root.resolve("config.yaml");
            Files.writeString(configFile, configContent);

            System.out.println("✅ Initialized dbgit repository for database: " + dbName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize dbgit repository", e);
        }
    }
}
