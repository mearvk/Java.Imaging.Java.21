# MySQL Integration (Optional)

This program works perfectly without a database. But if you'd like to track every file that's been organized, you can install MySQL and the program will automatically log operations to it.

## What It Gives You

With MySQL installed, the program records:
- Every file processed (original name, new name, type, date taken)
- Usage history (when the program was run, how many times)

Without MySQL, the program runs exactly the same — no errors, no warnings.

## Install MySQL

### Linux (Ubuntu/Debian)
```bash
sudo bash scripts/install-mysql-linux.sh
```

### macOS
```bash
bash scripts/install-mysql-macos.sh
```

### Windows (run as Administrator)
```cmd
scripts\install-mysql-windows.bat
```

Each script:
1. Installs MySQL server
2. Creates an `imaging` database
3. Creates tables (`file_log`, `usage_log`)
4. Sets up a user: `imaging` / `imaging2024`

## How It Integrates

Once MySQL is available on localhost:3306, the program automatically:
- Detects the database at startup
- Logs each moved file to `file_log`
- Records each run to `usage_log`

If MySQL is not running or not installed, the program skips database logging silently.

## Connection Details

| Setting | Value |
|---------|-------|
| Host | localhost |
| Port | 3306 |
| Database | imaging |
| User | imaging |
| Password | imaging2024 |
| JDBC URL | `jdbc:mysql://localhost:3306/imaging` |

## Schema

```sql
CREATE TABLE file_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(512),
    new_name VARCHAR(512),
    file_type VARCHAR(10),
    date_taken DATETIME,
    source_path VARCHAR(1024),
    dest_path VARCHAR(1024),
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usage_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    run_count INT,
    run_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## No MySQL Connector JAR Needed

The program uses Java's built-in `java.sql` package. MySQL Connector/J is loaded at runtime only if MySQL is present. If you installed MySQL via the scripts above, the connector is included automatically.

To manually add the connector:
1. Download from https://dev.mysql.com/downloads/connector/j/
2. Place `mysql-connector-j-*.jar` in the project root
3. Run with: `java -cp "out:mysql-connector-j-*.jar" pennywise.Main src/pennywise/config.xml`
