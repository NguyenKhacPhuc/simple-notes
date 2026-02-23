# Phase 04: UI Design System

## Goal
Define theme, colors, typography, and reusable Compose components.

## Theme (Compose Multiplatform)

### Color Tokens
**Light:**
- background: #FAF6F1 (warm cream)
- surface: #FFFFFF
- surfaceVariant: #F0EBE4
- onBackground: #1C1B18
- onBackgroundVariant: #4A4740
- primary: #6B5C4D (warm brown)
- primaryContainer: #F5DFC5
- error: #BA1A1A
- offlineBanner: #FFF0D1

**Dark:**
- background: #1C1B18 (warm charcoal)
- surface: #262420
- surfaceVariant: #33302B
- onBackground: #E8E2D9
- onBackgroundVariant: #9C9689
- primary: #D4B896
- primaryContainer: #4A3F32
- error: #FFB4AB
- offlineBanner: #3D3520

### Typography
- Note content: Source Serif 4 (bundle variable font wght 400-700)
- UI chrome: system font (Roboto/SF Pro)
- Title editor: Source Serif 4 SemiBold 24sp
- Body editor: Source Serif 4 Regular 17sp, line height 1.6x
- List title: System Medium 16sp
- List preview: System Regular 14sp

### Components to Build
1. `SimpleNotesTheme` — wraps MaterialTheme with custom color scheme + typography
2. `NoteListItem` — title + preview + timestamp row, swipe actions
3. `SearchBar` — M3 SearchBar with real-time filtering
4. `SyncStatusIndicator` — subtle spinning icon when syncing
5. `OfflineBanner` — warm amber banner below app bar
6. `FormattingToolbar` — bold/italic/strikethrough/bullet/checklist buttons
7. `EmptyState` — centered illustration + heading + subtitle

## Output Files
- `ui/theme/Color.kt`, `Type.kt`, `Theme.kt`
- `ui/components/` (shared composables)
- `composeApp/src/commonMain/composeResources/font/` (Source Serif 4)
