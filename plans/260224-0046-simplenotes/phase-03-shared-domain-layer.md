# Phase 03: Shared Domain Layer (MVI)

## Goal
Build the shared business logic in commonMain: models, repositories, ViewModels, sync manager.

## Package Structure
```
com.simplenotes.app/
  data/
    local/          # SQLDelight DAOs
    remote/         # Supabase client wrapper
    repository/     # NoteRepository (single source of truth)
    sync/           # SyncManager, SyncWorker
  domain/
    model/          # Note data class (domain model)
  ui/
    auth/           # AuthState, AuthIntent, AuthViewModel
    notes/          # NotesState, NotesIntent, NotesViewModel
    editor/         # EditorState, EditorIntent, EditorViewModel
    settings/       # SettingsState, SettingsIntent, SettingsViewModel
  di/               # Koin modules
```

## Key Components

### Models
- `Note`: id, title, body, createdAt, updatedAt, isDeleted, syncStatus
- `SyncStatus` enum: SYNCED, PENDING, CONFLICT

### NoteRepository
- Reads from SQLDelight (single source of truth)
- Writes to SQLDelight + enqueues sync action
- Exposes `Flow<List<Note>>` for reactive UI
- `search(query)`: FTS5 local search

### SyncManager
- `sync()`: push pending → pull remote → merge LWW
- `startRealtimeSync()`: subscribe to Supabase realtime channel
- Triggered on: app foreground, network reconnect, after local write (debounced)

### ViewModels (MVI)
- **NotesViewModel**: list, search, delete. State: notes list, query, loading, error
- **EditorViewModel**: load note, auto-save (debounce 1s), create new. State: title, body, isSaving
- **AuthViewModel**: sign in, sign up, sign out. State: user, loading, error
- **SettingsViewModel**: account info, sync status, sign out

### DI (Koin)
- `commonModule`: repository, sync manager, supabase client, ViewModels
- `androidModule`: Android SQLite driver
- `iosModule`: Native SQLite driver

## Depends On
- Phase 01 (project structure)
- Phase 02 (database schemas)
