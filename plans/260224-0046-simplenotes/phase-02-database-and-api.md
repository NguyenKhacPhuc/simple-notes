# Phase 02: Database & API

## Goal
Set up Supabase schema, RLS, and local SQLDelight database.

## Steps

### 1. Supabase CLI init
- `supabase init` in project root
- `supabase link --project-ref gqedjehqxijaxsrqmduk`

### 2. Migrations
Create 3 migration files:

**001_create_notes.sql:**
- `notes` table: id (UUID PK), user_id (FK auth.users), title, content, is_deleted, sync_version, created_at, updated_at
- Generated `fts` tsvector column (title weight A, content weight B)
- Indexes: user_id, updated_at, GIN on fts
- `set_updated_at()` trigger: auto-bumps updated_at and sync_version

**002_rls_policies.sql:**
- Enable RLS on notes
- 4 policies: select/insert/update/delete scoped to auth.uid() = user_id

**003_search_rpc.sql:**
- `search_notes(query text)` function using plainto_tsquery + ts_rank

### 3. Apply migrations
- `supabase db push`

### 4. SQLDelight local schema
Create `.sq` files in commonMain:

**Note.sq:**
- `Note` table: id TEXT PK, user_id, title, body, is_deleted, sync_version, created_at, updated_at, sync_status
- `NoteFts` FTS5 virtual table on title+body
- Triggers: after insert/update/delete to keep FTS in sync
- Queries: getAll, getById, upsert, markDeleted, searchNotes (via FTS)

**SyncQueue.sq:**
- `sync_queue` table: id INTEGER PK AUTOINCREMENT, note_id, action, payload (JSON), queued_at
- Queries: allPending, insert, delete

**SyncMeta.sq:**
- `sync_meta` table: key TEXT PK, value TEXT
- Store lastSyncTimestamp

## Output Files
- `supabase/migrations/` (3 SQL files)
- `composeApp/src/commonMain/sqldelight/` (.sq files)
