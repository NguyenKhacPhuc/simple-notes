# Backend Architecture Research -- SimpleNotes (supabase-kt)

Project ref: `gqedjehqxijaxsrqmduk` | URL: `https://gqedjehqxijaxsrqmduk.supabase.co`

## 1. Database Schema

```sql
-- Migration: 001_create_notes.sql
create extension if not exists "uuid-ossp";

create table public.notes (
  id          uuid primary key default uuid_generate_v4(),
  user_id     uuid not null references auth.users(id) on delete cascade,
  title       text not null default '',
  content     text not null default '',
  is_deleted  boolean not null default false,
  sync_version bigint not null default 1,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  fts         tsvector generated always as (
    setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(content, '')), 'B')
  ) stored
);

create index idx_notes_user_id    on public.notes(user_id);
create index idx_notes_updated_at on public.notes(updated_at);
create index idx_notes_fts        on public.notes using gin(fts);

-- Auto-update updated_at
create or replace function public.set_updated_at()
returns trigger as $$ begin
  new.updated_at = now();
  new.sync_version = old.sync_version + 1;
  return new;
end; $$ language plpgsql;

create trigger trg_notes_updated_at
  before update on public.notes
  for each row execute function public.set_updated_at();
```

## 2. RLS Policies

```sql
alter table public.notes enable row level security;

create policy "Users read own notes"  on public.notes for select using (auth.uid() = user_id);
create policy "Users insert own notes" on public.notes for insert with check (auth.uid() = user_id);
create policy "Users update own notes" on public.notes for update using (auth.uid() = user_id);
create policy "Users delete own notes" on public.notes for delete using (auth.uid() = user_id);
```

## 3. Full-Text Search RPC

```sql
create or replace function public.search_notes(query text)
returns setof public.notes
language sql stable security definer
set search_path = '' as $$
  select * from public.notes
  where user_id = auth.uid()
    and is_deleted = false
    and fts @@ plainto_tsquery('english', query)
  order by ts_rank(fts, plainto_tsquery('english', query)) desc
  limit 50;
$$;
```

## 4. Auth Strategy

Email/password via Supabase Auth. supabase-kt `Auth` module handles token refresh automatically and persists sessions per-platform via `SessionManager`.

```kotlin
// Shared KMP client init
val supabase = createSupabaseClient(
    supabaseUrl = "https://gqedjehqxijaxsrqmduk.supabase.co",
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}

// Sign up / sign in
suspend fun signUp(email: String, password: String) {
    supabase.auth.signUpWith(Email) { this.email = email; this.password = password }
}
suspend fun signIn(email: String, password: String) {
    supabase.auth.signInWith(Email) { this.email = email; this.password = password }
}
```

## 5. Postgrest Queries (supabase-kt)

```kotlin
@Serializable
data class NoteDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val content: String,
    @SerialName("is_deleted") val isDeleted: Boolean,
    @SerialName("sync_version") val syncVersion: Long,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

// Fetch notes updated after a timestamp (for sync pull)
suspend fun pullRemoteChanges(since: String): List<NoteDto> =
    supabase.from("notes")
        .select { filter { gt("updated_at", since) } }
        .decodeList()

// Upsert a note (push)
suspend fun upsertNote(note: NoteDto) {
    supabase.from("notes").upsert(note) { onConflict = "id" }
}

// Full-text search via RPC
suspend fun searchNotes(query: String): List<NoteDto> =
    supabase.functions  // or postgrest rpc:
    supabase.from("rpc/search_notes")  // alternative:
    supabase.postgrest.rpc("search_notes", mapOf("query" to query)).decodeList()
```

## 6. Realtime Subscription

```kotlin
val channel = supabase.realtime.channel("notes-changes")

val notesFlow = channel.postgresChangeFlow<PostgresAction>("public") {
    table = "notes"
    filter = "user_id=eq.${supabase.auth.currentUserOrNull()?.id}"
}

// Collect in a coroutine scope
notesFlow.collect { action ->
    when (action) {
        is PostgresAction.Insert -> onRemoteInsert(action.decodeRecord<NoteDto>())
        is PostgresAction.Update -> onRemoteUpdate(action.decodeRecord<NoteDto>())
        is PostgresAction.Delete -> onRemoteDelete(action.decodeOldRecord<NoteDto>())
        else -> {}
    }
}
channel.subscribe()
```

## 7. Offline Sync Pattern (LWW + Local Queue)

**Local queue table** (SQLDelight):

```sql
-- sqldelight: SyncQueue.sq
CREATE TABLE sync_queue (
  id        INTEGER PRIMARY KEY AUTOINCREMENT,
  note_id   TEXT NOT NULL,
  action    TEXT NOT NULL, -- CREATE | UPDATE | DELETE
  payload   TEXT NOT NULL, -- JSON-serialized NoteDto
  queued_at TEXT NOT NULL DEFAULT (datetime('now'))
);
```

**Sync algorithm** (Kotlin):

```kotlin
class SyncManager(
    private val local: NoteDao,        // SQLDelight DAO
    private val remote: RemoteApi,     // Postgrest wrapper above
    private val queue: SyncQueueDao,
    private val prefs: SyncPrefs       // stores lastSyncTimestamp
) {
    suspend fun sync() {
        pushPendingChanges()
        pullRemoteChanges()
    }

    private suspend fun pushPendingChanges() {
        val pending = queue.allPending()
        for (entry in pending) {
            when (entry.action) {
                "CREATE", "UPDATE" -> remote.upsertNote(entry.toNoteDto())
                "DELETE" -> remote.upsertNote(entry.toNoteDto().copy(isDeleted = true))
            }
            queue.delete(entry.id)
        }
    }

    private suspend fun pullRemoteChanges() {
        val since = prefs.lastSyncTimestamp ?: "1970-01-01T00:00:00Z"
        val remoteNotes = remote.pullRemoteChanges(since)
        for (note in remoteNotes) {
            val localNote = local.getById(note.id)
            // LWW: remote wins if its updated_at is newer
            if (localNote == null || note.updatedAt > localNote.updatedAt) {
                local.upsert(note.toLocalEntity())
            }
        }
        prefs.lastSyncTimestamp = remoteNotes.maxOfOrNull { it.updatedAt } ?: since
    }
}
```

## 8. Migration File Layout (Supabase CLI)

```
supabase/
  migrations/
    20260224000001_create_notes.sql      -- schema + indexes + trigger
    20260224000002_rls_policies.sql       -- RLS policies
    20260224000003_search_rpc.sql         -- search_notes function
```

Apply with: `supabase db push` (remote) or `supabase migration up` (local).

---

**Key decisions**: LWW on `updated_at` is chosen for simplicity; `sync_version` bigint enables future optimistic concurrency if needed. Soft-delete (`is_deleted`) keeps tombstones so clients can detect remote deletions during pull. The generated `fts` column avoids manual tsvector management.
