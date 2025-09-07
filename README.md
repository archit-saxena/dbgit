
# DBGit — A Git-like CLI Tool for MySQL Database Versioning

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.java.com/) [![MySQL](https://img.shields.io/badge/MySQL-8.0-green)](https://www.mysql.com/)

`DBGit` is a lightweight command-line tool to manage versioning of MySQL **schema and data**, inspired by Git.
It supports **tracking tables**, **snapshotting database state (commit)**, and lays the foundation for future features like **revert** and **diff**.

---

## ✅ Current Features

### ⚡ 1. `init`

Initializes the `.dbgit/` folder and creates a configuration file.

```bash
java -jar dbgit.jar init --db <db_name>
```

This command creates a `.dbgit/` directory in the current working directory, containing:

* `config.yaml` – Configuration file with database connection details and tracked tables
* `commits/` – Directory to store snapshots of tracked tables

**Options:**

| Option                  | Description                 | Default   |
| ----------------------- | --------------------------- | --------- |
| `--db <db_name>`        | Database name to connect to | **Required**       |
| `--host <host>`         | Database host               | localhost |
| `--user <username>`     | Database username           | root       |
| `--password <password>` | Database password           |        |
| `--port <port>`         | Database port               | 3306      |

---

### ⚡ 2. `track <table_name>`

Mark, remove, or list tables to be tracked for versioning. Tracked tables are saved in `.dbgit/config.yaml`.

```bash
java -jar dbgit.jar track add <table_name>
java -jar dbgit.jar track remove <table_name>
java -jar dbgit.jar track list
```

---

### ⚡ 3. `change-db <new_db_name>`

Change the database in the configuration file after validating that it exists.

```bash
java -jar dbgit.jar change-db new_database_name
```

---

### ⚡ 4. `commit -m "message"`

Creates a new commit by snapshotting the schema and data of all tracked tables into `.dbgit/commits/`.

```bash
java -jar dbgit.jar commit -m "<commit_message"
```

* **Schema** is saved as `.sql`
* **Data** is saved as `.json`
* **Metadata** (commit message + timestamp) is saved in `commit_metadata.yaml`

---

## ✅ Folder Structure After `init` and `commit`

```
.dbgit/
├── config.yaml
└── commits/
    └── <db_name>/
        └── 0001_<commit_message>/
            ├── schema/
            │   └── <table_name>.sql
            ├── data/
            │   └── <table_name>.json
            └── commit_metadata.yaml
```

---

## ✅ Future Prospects

| Feature                        | Description                                                   |
| ------------------------------ | ------------------------------------------------------------- |
| **Revert** (in development)    | Rollback schema & data to a previous commit                   |
| **Diff** (in development)      | Compare two commits and show differences in schema and data   |
| Incremental Commits (Optional) | Store only changes instead of full snapshots for optimization |
| CLI Packaging                  | Create installable `.deb` or Homebrew package                 |

---

## ✅ Setup Instructions

1. Install **Java** (17+) and **MySQL**.
2. Build the project using Maven:

```bash
mvn clean package
```

3. Run the CLI using:

```bash
java -jar target/dbgit-1.0.jar <command>
```

---

## ✅ Contributing

Feel free to:

* Open issues for bugs or feature requests
* Submit pull requests for improvements
* Suggest new ideas for database versioning workflows

---
