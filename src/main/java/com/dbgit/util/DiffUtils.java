package com.dbgit.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.dbgit.util.ConfigUtils;

public class DiffUtils {

    public static void diff(String commitId1, String commitId2, boolean schemaOnly, boolean dataOnly) throws IOException {
        String dbName = ConfigUtils.getActiveDatabase();

        Path commit1Dir = Paths.get(".dbgit/commits", dbName, getCommitFolder(commitId1));
        Path commit2Dir = Paths.get(".dbgit/commits", dbName, getCommitFolder(commitId2));

        if (!Files.exists(commit1Dir) || !Files.exists(commit2Dir))
            throw new RuntimeException("One or both commits not found");

        List<String> trackedTables = ConfigUtils.readConfig().tracked_tables;
        boolean differencesFound = false;

        for (String table : trackedTables) {
            if (!dataOnly) {
                String schema1 = Files.readString(commit1Dir.resolve("schema").resolve(table + ".sql"));
                String schema2 = Files.readString(commit2Dir.resolve("schema").resolve(table + ".sql"));
                if (!schema1.equals(schema2)) {
                    differencesFound = true;
                    System.out.println("\nSchema differences for table: " + table);
                    showLineDiff(schema1, schema2);
                }
            }

            if (!schemaOnly) {
                String data1 = Files.readString(commit1Dir.resolve("data").resolve(table + ".json"));
                String data2 = Files.readString(commit2Dir.resolve("data").resolve(table + ".json"));
                if (!normalizeJson(data1).equals(normalizeJson(data2))) {
                    differencesFound = true;
                    System.out.println("\nData differences for table: " + table);
                    showJsonDiff(data1, data2);
                }
            }
        }

        if (!differencesFound)
            System.out.println("No differences between commits " + commitId1 + " and " + commitId2);
    }

    // -------------------- Utilities --------------------

    private static String normalizeJson(String jsonStr) {
        try {
            JSONArray arr = new JSONArray(jsonStr);
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++)
                list.add(arr.getJSONObject(i));
            list.sort(Comparator.comparing(JSONObject::toString));
            return list.toString();
        } catch (Exception e) {
            return jsonStr.trim();
        }
    }

    private static void showLineDiff(String oldText, String newText) {
        String[] oldLines = oldText.split("\n");
        String[] newLines = newText.split("\n");

        Set<String> oldSet = new HashSet<>();
        Set<String> newSet = new HashSet<>();

        for (String line : oldLines) {
            if (!line.contains("AUTO_INCREMENT"))
                oldSet.add(line);
        }

        for (String line : newLines) {
            if (!line.contains("AUTO_INCREMENT"))
                newSet.add(line);
        }

        for (String line : oldSet)
            if (!newSet.contains(line))
                System.out.println("- " + line);
        for (String line : newSet)
            if (!oldSet.contains(line))
                System.out.println("+ " + line);
    }


    private static void showJsonDiff(String oldJson, String newJson) {
        try {
            JSONArray oldArr = new JSONArray(oldJson);
            JSONArray newArr = new JSONArray(newJson);

            Set<String> oldSet = new HashSet<>();
            Set<String> newSet = new HashSet<>();

            for (int i = 0; i < oldArr.length(); i++)
                oldSet.add(oldArr.getJSONObject(i).toString());
            for (int i = 0; i < newArr.length(); i++)
                newSet.add(newArr.getJSONObject(i).toString());

            // compute added and removed rows
            Set<String> added = new HashSet<>(newSet);
            added.removeAll(oldSet);

            Set<String> removed = new HashSet<>(oldSet);
            removed.removeAll(newSet);

            if (!removed.isEmpty()) {
                System.out.println(removed.size() + " row(s) deleted:");
                for (String row : removed) {
                    JSONObject obj = new JSONObject(row);
                    System.out.println("- " + prettyJson(obj));
                }
            }

            if (!added.isEmpty()) {
                System.out.println(added.size() + " row(s) added:");
                for (String row : added) {
                    JSONObject obj = new JSONObject(row);
                    System.out.println("+ " + prettyJson(obj));
                }
            }

            if (added.isEmpty() && removed.isEmpty()) {
                System.out.println("No data differences found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to diff JSON: " + e.getMessage());
        }
    }

    private static String prettyJson(JSONObject obj) {
        StringBuilder sb = new StringBuilder("{ ");
        String[] keys = JSONObject.getNames(obj);
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                sb.append(keys[i]).append(": ").append(obj.get(keys[i]));
                if (i < keys.length - 1) sb.append(", ");
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    private static String getCommitFolder(String commitId) throws IOException {
        String dbName = ConfigUtils.getActiveDatabase();
        Path commitsDir = Paths.get(".dbgit/commits", dbName);
        if (!Files.exists(commitsDir))
            throw new RuntimeException("No commits found for database: " + dbName);

        try (var paths = Files.list(commitsDir)) {
            return paths.map(p -> p.getFileName().toString())
                    .filter(name -> name.startsWith(commitId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Commit folder not found for: " + commitId));
        }
    }
}
