# SimpleNotes KMP Research Report

**Date:** 2026-02-24
**Scope:** KMP note-taking app -- Android + iOS, offline-first, search, Supabase sync

> **Note:** Library versions below are based on the latest known stable releases as of early 2025. Verify against Maven Central / GitHub Releases before locking your version catalog.

---

## 1. KMP Project Structure (Compose Multiplatform)

Use the JetBrains KMP Wizard (`kmp.jetbrains.com`) to scaffold. Target structure:

```
SimpleNotes/
  composeApp/          # Shared Compose UI + logic (commonMain, androidMain, iosMain)
  server/              # (optional) Edge functions live in Supabase, not here
  iosApp/              # Thin SwiftUI wrapper hosting ComposeView
  gradle/
    libs.versions.toml # Single version catalog
  build.gradle.kts
  settings.gradle.kts
```

**Source sets inside `composeApp`:**

| Source Set    | Purpose                                         |
|---------------|--------------------------------------------------|
| `commonMain`  | UI (Compose), ViewModels, Repository, DB schema  |
| `androidMain` | Android SQLite driver, Android-specific DI        |
| `iosMain`     | Native SQLite driver, NSUserDefaults bindings      |

Compose Multiplatform now ships stable iOS rendering (Skiko-backed). Use a single `@Composable` entry point in commonMain; the iosApp project wraps it via `ComposeUIViewController`.

---

## 2. Key Libraries -- Version Catalog

```toml
# gradle/libs.versions.toml
[versions]
kotlin          = "2.1.10"
compose-plugin  = "1.7.3"       # JetBrains Compose Multiplatform plugin
sqldelight      = "2.0.2"
ktor            = "3.1.0"
serialization   = "1.7.3"
koin            = "4.0.2"
supabase-kt     = "3.1.1"       # io.github.jan-tennert.supabase BOM
coroutines      = "1.10.1"
datetime        = "0.6.2"

[libraries]
sqldelight-android   = { module = "app.cash.sqldelight:android-driver",    version.ref = "sqldelight" }
sqldelight-native    = { module = "app.cash.sqldelight:native-driver",     version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
ktor-client-core     = { module = "io.ktor:ktor-client-core",              version.ref = "ktor" }
ktor-client-content   = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization    = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp   = { module = "io.ktor:ktor-client-okhttp",            version.ref = "ktor" }
ktor-client-darwin   = { module = "io.ktor:ktor-client-darwin",             version.ref = "ktor" }
supabase-bom         = { module = "io.github.jan-tennert.supabase:bom",     version.ref = "supabase-kt" }
supabase-postgrest   = { module = "io.github.jan-tennert.supabase:postgrest-kt" }
supabase-realtime    = { module = "io.github.jan-tennert.supabase:realtime-kt" }
supabase-auth        = { module = "io.github.jan-tennert.supabase:auth-kt" }
koin-core            = { module = "io.insert-koin:koin-core",               version.ref = "koin" }
koin-compose         = { module = "io.insert-koin:koin-compose",            version.ref = "koin" }
kotlinx-datetime     = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
kotlinx-serialization = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-coroutines   = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform",        version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose",                    version.ref = "compose-plugin" }
compose-compiler     = { id = "org.jetbrains.kotlin.plugin.compose",       version.ref = "kotlin" }
sqldelight           = { id = "app.cash.sqldelight",                       version.ref = "sqldelight" }
```

---

## 3. MVI Architecture (Manual, Lightweight)

No framework needed. Three files per feature:

```
feature/notes/
  NotesState.kt      -- data class (immutable)
  NotesIntent.kt     -- sealed interface
  NotesViewModel.kt  -- reduces intents into state
```

**Pattern:**

```kotlin
// State
data class NotesState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// Intents
sealed interface NotesIntent {
    data class Search(val query: String) : NotesIntent
    data class Delete(val id: String) : NotesIntent
    data object Refresh : NotesIntent
}

// ViewModel (extends KMP ViewModel from lifecycle-viewmodel-compose)
class NotesViewModel(private val repo: NoteRepository) : ViewModel() {
    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    fun send(intent: NotesIntent) { viewModelScope.launch { reduce(intent) } }

    private suspend fun reduce(intent: NotesIntent) = when (intent) {
        is NotesIntent.Search -> {
            _state.update { it.copy(query = intent.query, isLoading = true) }
            val results = repo.search(intent.query)
            _state.update { it.copy(notes = results, isLoading = false) }
        }
        // ...
    }
}
```

Use `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` (ships with Compose Multiplatform) for the shared ViewModel.

---

## 4. Offline-First Sync Pattern

```
[SQLDelight local DB] <--read/write-- [Repository] --enqueue--> [SyncQueue table]
                                                                       |
                                                              SyncWorker (push)
                                                                       |
                                                                  [Supabase]
                                                                       |
                                                              SyncWorker (pull)
                                                                       |
                                                             [SQLDelight merge]
```

**Key tables (SQLDelight `.sq` files):**

- `Note(id TEXT PK, title, body, updated_at TEXT, is_deleted INT, sync_status TEXT)`
- `SyncQueue(id INTEGER PK AUTOINCREMENT, note_id TEXT, action TEXT, created_at TEXT)`

**sync_status** values: `synced`, `pending`, `conflict`.

**Push flow:** Worker reads SyncQueue, calls `supabase.postgrest["notes"].upsert(...)`, on success deletes queue entry and sets `sync_status = synced`.

**Pull flow:** Fetch rows from Supabase where `updated_at > last_pull_timestamp`. Merge using last-write-wins on `updated_at`. Store `last_pull_timestamp` in a `SyncMeta` table.

**Conflict strategy:** Last-write-wins (LWW) by `updated_at` (ISO-8601 via `kotlinx-datetime`). Good enough for a personal notes app.

---

## 5. Search Strategy

### Local (offline) -- SQLDelight FTS5

```sql
-- In Note.sq
CREATE VIRTUAL TABLE NoteFts USING fts5(title, body, content=Note, content_rowid=rowid);

-- Triggers to keep FTS in sync
CREATE TRIGGER note_ai AFTER INSERT ON Note BEGIN
  INSERT INTO NoteFts(rowid, title, body) VALUES (new.rowid, new.title, new.body);
END;

-- Search query
searchNotes:
SELECT Note.* FROM Note
JOIN NoteFts ON Note.rowid = NoteFts.rowid
WHERE NoteFts MATCH :query
ORDER BY rank;
```

### Remote -- Supabase Postgres Full-Text Search

```kotlin
val results = supabase.postgrest["notes"]
    .select {
        filter { textSearch("fts", query, TextSearchType.WEBSEARCH) }
    }
    .decodeList<NoteDto>()
```

Requires a `tsvector` column + GIN index on the Supabase side:

```sql
ALTER TABLE notes ADD COLUMN fts tsvector
  GENERATED ALWAYS AS (to_tsvector('english', coalesce(title,'') || ' ' || coalesce(body,''))) STORED;
CREATE INDEX notes_fts_idx ON notes USING GIN(fts);
```

Search locally first (instant), then merge remote results in the background if online.

---

## Summary of Recommendations

| Concern          | Choice                                    |
|------------------|-------------------------------------------|
| UI               | Compose Multiplatform (single codebase)    |
| Architecture     | Manual MVI -- State/Intent/Reducer         |
| Local DB         | SQLDelight (type-safe, KMP-native)         |
| Networking       | Ktor Client (per-platform engines)         |
| Backend          | Supabase (Postgrest + Auth + Realtime)     |
| DI               | Koin (lightweight, KMP-ready)              |
| Sync             | Offline-first queue + LWW merge            |
| Search           | FTS5 local + Postgres tsvector remote      |
