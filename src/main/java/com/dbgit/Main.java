package com.dbgit;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import com.dbgit.commands.InitCommand;
import com.dbgit.commands.TrackCommand;

@Command(
        name = "dbgit",
        mixinStandardHelpOptions = true,
        version = { "dbgit 0.1.0" },
        description = "Git-like CLI for MySQL (commit/diff/revert).",
        subcommands = { InitCommand.class,
                        TrackCommand.class
        }
)
public class Main implements Runnable {

    public static void main(String[] args) {
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }

    @Override
    public void run() {
        // When no subcommand/options are given, show help
        new CommandLine(this).usage(System.out);
    }
}
