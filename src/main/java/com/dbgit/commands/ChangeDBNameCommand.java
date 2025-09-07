package com.dbgit.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.dbgit.model.Config;
import com.dbgit.util.ConfigUtils;
import com.dbgit.util.DatabaseUtils;

@Command(name = "change-db", description = "Change the database in config.yaml")
public class ChangeDBNameCommand implements Runnable {

    @Parameters(index = "0", description = "New database name to switch to")
    String newDbName;

    @Override
    public void run() {
        Config config = ConfigUtils.readConfig();

        // Check if new database exists
        if (!DatabaseUtils.databaseExists(config.database, newDbName)) {
            System.out.println("[X] Database does not exist: " + newDbName);
            return;
        }

        // Update config
        config.database.name = newDbName;
        ConfigUtils.writeConfig(config);
        System.out.println("[S] Database changed to: " + newDbName);
    }
}
