package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import com.dbgit.model.Config;
import com.dbgit.util.DatabaseUtils;
import com.dbgit.util.ConfigUtils;

@Command(name = "change-db", description = "Change the database in config.yaml")
public class ChangeDBNameCommand implements Runnable {

    @Parameters(index = "0", description = "New database name to switch to")
    String newDbName;

    @Override
    public void run() {
        Config cfg = Config.getInstance();

        // Check if new database exists
        if (!DatabaseUtils.databaseExists(newDbName)) {
            System.out.println("[X] Database does not exist: " + newDbName);
            return;
        }

        // Update config
        cfg.database.name = newDbName;
        ConfigUtils.writeConfig(cfg);

        System.out.println("[S] Database changed to: " + newDbName);
    }
}
