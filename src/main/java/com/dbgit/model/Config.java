package com.dbgit.model;

import java.util.ArrayList;
import java.util.List;
import com.dbgit.util.ConfigUtils;

public class Config {
    private static Config instance;

    public Database database = new Database();
    public List<String> tracked_tables = new ArrayList<>();

    private Config() { }

    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = ConfigUtils.readConfig();
                }
            }
        }
        return instance;
    }

    public static void reload() {
        instance = ConfigUtils.readConfig();
    }

    public static Config createEmpty() {
        return new Config();
    }

    public static class Database {
        public String host;
        public int port;
        public String name;
        public String user;
        public String password;
    }
}
