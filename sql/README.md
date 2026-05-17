# Database Setup Guide

This project uses MySQL 8+. The schema and demo scripts are separated so you can choose whether to load only structure or a full demo dataset.

## Files

- `unique_finds_full_schema.sql`: full schema, triggers, and base dictionary data
- `demo_seed_data.sql`: demo users, posts, images, interactions, and moderation data
- `community_interaction_patch.sql`: interaction patch
- `community_interaction_validation.sql`: migration validation and count consistency checks for the interaction patch
- `moderation_governance_patch.sql`: moderation patch
- `src/main/resources/db/migration/`: Flyway-managed incremental migrations used by the Spring Boot app

The latest backend phase relies on Flyway `V3__moderation_audit_and_interaction_events.sql` to patch existing environments with:

- `reports.resolution_action`
- `reports.resolution_note`
- richer `interaction_events` columns for `comment_id`, `target_type`, `target_id`, and JSON `metadata`

## Import Order

### Option A: MySQL CLI

```bash
mysql -u <username> -p < "sql/unique_finds_full_schema.sql"
mysql -u <username> -p < "sql/demo_seed_data.sql"
```

### Option B: DataGrip / Navicat / MySQL Workbench

1. Run `unique_finds_full_schema.sql`
2. Run `demo_seed_data.sql`
3. Start the backend once so Flyway can baseline and validate the incremental migrations
4. Confirm `unique_finds` database and all expected tables are present

## Existing Environment Upgrade

For an existing database that already has the base schema, start the backend directly. Flyway is configured with `baseline-on-migrate=true`, so it can adopt the existing schema history without dropping data.

If the existing database still lacks the social interaction objects, run the reference patch once before the first Flyway-managed startup:

```bash
mysql -u <username> -p unique_finds < "sql/community_interaction_patch.sql"
```

After the patch finishes, run the validation script:

```bash
mysql -u <username> -p unique_finds < "sql/community_interaction_validation.sql"
```

The validation script checks:

- `comments.is_pinned` exists
- `comment_likes` exists and updates `comments.like_count`
- `notifications` exists
- `notifications.metadata` exists after Flyway V2
- `reports` includes moderation resolution audit fields after Flyway V3
- `interaction_events` accepts search, share, and report-close style events after Flyway V3
- `posts.comment_count` stays consistent when comment visibility changes
- pinned comments sort ahead of non-pinned comments
- all agreed notification event types can be stored

## Demo Notes

- Demo accounts are seeded by `demo_seed_data.sql`
- Default demo password: `Password123`
- The seed data includes:
  - published posts
  - pending / rejected / hidden moderation states
  - likes, favorites, comments
  - report and moderation records
  - trending-friendly engagement distribution

## Notes

- The schema script is idempotent for core table creation.
- Triggers are recreated with `DROP TRIGGER IF EXISTS ...` before `CREATE TRIGGER`.
- The interaction patch is safe to rerun when tables and triggers already exist.
- Flyway owns new incremental changes after the baseline schema is in place.
- Re-running the demo seed may require cleaning conflicting custom demo data first if you manually changed rows with the same IDs.
