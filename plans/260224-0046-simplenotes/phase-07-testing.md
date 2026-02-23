# Phase 07: Testing

## Goal
Unit tests for shared domain logic. Target: core sync and repository logic covered.

## Test Scope

### Unit Tests (commonTest)
1. **NoteRepository tests**: CRUD operations, search returns correct results
2. **SyncManager tests**: push enqueues correctly, pull merges LWW correctly, conflict resolution
3. **ViewModel tests**: intent → state transitions for NotesViewModel, EditorViewModel, AuthViewModel
4. **Model mapping tests**: NoteDto ↔ Note entity conversion

### Integration (manual)
- Auth flow: sign up → sign in → session persists across app restart
- Offline: airplane mode → create note → reconnect → note syncs
- Search: create notes with known content → search finds them
- Realtime: edit on second device → first device updates

## Test Libraries
- kotlin.test (KMP built-in)
- kotlinx-coroutines-test for ViewModel/Flow testing
- Turbine for Flow assertions

## Output Files
- `composeApp/src/commonTest/kotlin/.../`
