# Database Setup Guide

This project uses MySQL 8+. The schema file is idempotent and can be re-run safely.

## Files

- `unique_finds_full_schema.sql`: full schema + triggers + seed data.

## Option A: MySQL CLI

```bash
mysql -u <username> -p < "sql/unique_finds_full_schema.sql"
```

After import, verify:

```sql
USE unique_finds;
SHOW TABLES;
```

## Option B: DataGrip / Navicat / MySQL Workbench

1. Open `sql/unique_finds_full_schema.sql`.
2. Run the full script.
3. Confirm `unique_finds` database and all tables are present.

## Notes

- The script includes `CREATE TABLE IF NOT EXISTS`.
- Triggers are recreated with `DROP TRIGGER IF EXISTS ...` before `CREATE TRIGGER`.
- Re-running the script will not break existing schema.
