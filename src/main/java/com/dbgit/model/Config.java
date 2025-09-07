package com.dbgit.model;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public Database database = new Database();
    public List<String> tracked_tables = new ArrayList<>();

    public static class Database {
        public String host;
        public int port;
        public String name;
        public String user;
        public String password;
    }
}
