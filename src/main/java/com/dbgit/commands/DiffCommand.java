package com.dbgit.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;
import com.dbgit.util.DiffUtils;
import com.dbgit.util.HeadUtils;

import java.util.List;

@Command(name = "diff", description = "Show differences between commits")
public class DiffCommand implements Runnable {

    @Parameters(arity = "1..2", description = "Commit IDs to compare")
    List<String> commits;

    @Option(names = "--schema-only", description = "Show only schema differences")
    boolean schemaOnly;

    @Option(names = "--data-only", description = "Show only data differences")
    boolean dataOnly;

    @Override
    public void run() {
        try {
            if (schemaOnly && dataOnly) {
                System.out.println("[X] You cannot use both --schema-only and --data-only together.");
                return;
            }

            String commit1 = commits.get(0);
            String commit2 = commits.size() == 2 ? commits.get(1) : HeadUtils.getHead();

            DiffUtils.diff(commit1, commit2, schemaOnly, dataOnly);
        } catch (Exception e) {
            System.err.println("[X] Diff failed: " + e.getMessage());
        }
    }
}
