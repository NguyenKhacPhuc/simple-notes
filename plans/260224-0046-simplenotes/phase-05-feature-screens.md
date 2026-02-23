# Phase 05: Feature Screens

## Goal
Implement all 4 screens using shared Compose Multiplatform.

## Screens

### 1. AuthScreen
- Two tabs/toggle: Sign In / Sign Up
- Email + password fields
- Submit button with loading state
- Error display (inline below fields)
- On success → navigate to NoteListScreen

### 2. NoteListScreen
- Top: SearchBar (M3, real-time filtering)
- Pinned section (if any pinned notes) with "Pinned" divider
- Note list: NoteListItem rows sorted by updated_at desc
- Swipe-left: delete (with undo snackbar 5s)
- Long-press / context menu: pin/unpin
- FAB (Android) / toolbar + button (iOS, via expect/actual): new note
- SyncStatusIndicator in top bar
- OfflineBanner when disconnected
- EmptyState when no notes

### 3. NoteEditorScreen
- Title field (large, placeholder "Title")
- Body field (full height, Source Serif 4)
- Auto-save: debounce 1s on text change
- Bottom: FormattingToolbar (above keyboard)
- Back navigation auto-saves
- New note: auto-title from first line if title left empty

### 4. SettingsScreen
- User email display
- Sync status: last synced timestamp
- Manual sync button
- Sign out button (with confirmation dialog)
- App version

## Navigation
- Use Compose Navigation (multiplatform)
- Routes: auth, notes, editor/{noteId?}, settings
- Auth gate: if no session → auth screen, else → notes screen

## Depends On
- Phase 03 (ViewModels, repositories)
- Phase 04 (design system, components)
