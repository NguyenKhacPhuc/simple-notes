# SimpleNotes UX/UI Research Report

## 1. Core UX Patterns

**Note List (Simple List)**
- Single-column list with title (bold, 1 line) + preview snippet (regular, 2 lines max) + timestamp.
- Sort by last-modified descending. No manual reordering -- KISS.
- Swipe actions: swipe-right to pin/unpin, swipe-left to delete (with undo snackbar, 5s).
- Pinned notes section at top with a subtle divider label ("Pinned").

**Note Editor**
- Full-screen editor. Title field at top (large, no label -- placeholder "Title"). Body below.
- Auto-save on every pause (debounce 1s). No explicit save button.
- Basic formatting only: **bold**, *italic*, ~~strikethrough~~, bullet list, checklist. Compact toolbar at bottom above keyboard.
- Word/character count in a subtle footer -- tap to toggle.

**Search**
- Persistent search bar at top of the note list (M3 SearchBar / iOS `.searchable` modifier).
- Real-time filtering as user types, highlighting matched terms in results.
- Empty search state: "No notes match your search" with a clear-filter action.

**Empty States**
- First launch: centered illustration (simple line art), heading "No notes yet", subtitle "Tap + to start writing".
- Empty search: "No results" with the query echoed back.
- Empty trash: "Trash is empty" -- no action needed.

## 2. Material Design 3 (Android)

- **Dynamic Color**: Use `dynamicColorScheme()` on Android 12+. Fall back to the warm neutral seed palette (see Section 7) on older versions.
- **Key Components**: `TopAppBar` (small, collapsing on scroll), `FloatingActionButton` (tertiary container color, bottom-end), `SearchBar` (docked at top, expands to `SearchView`), `Snackbar` for undo/sync toasts, `ModalBottomSheet` for formatting toolbar overflow.
- **Typography**: Use `MaterialTheme.typography` with custom `bodyLarge`/`bodyMedium` mapped to Source Serif 4 for note content. Keep UI chrome (labels, buttons) on the default M3 system font.
- **Shape**: Use M3 medium rounding (12dp) for cards/sheets. Note list items are full-width rows, not cards -- keep it flat to reduce visual noise.

## 3. iOS Human Interface Guidelines

- **NavigationStack**: Use `NavigationStack` with `NavigationLink` for list-to-detail push. Standard back chevron, no custom back buttons.
- **Search**: `.searchable(text:)` modifier on the list view. Uses native pull-to-reveal search bar behavior.
- **SwiftUI Conventions**: `List` with `.listStyle(.insetGrouped)` for the note list. Use `@Environment(\.editMode)` for multi-select/delete. Context menus via `.contextMenu` for pin/share/delete.
- **SF Symbols**: Use `square.and.pencil` for new note, `pin.fill` for pinned, `trash` for delete, `arrow.triangle.2.circlepath` for sync.
- **Platform Feel**: No FAB on iOS. Use a toolbar button (top-right `+`) for new note. Respect safe areas and large title navigation bars.

## 4. Competitor Analysis -- What Works

| App | Key Strength | Steal This |
|---|---|---|
| **Apple Notes** | Zero-friction start: tap +, start typing, done. Title inferred from first line. | Auto-title from first line. No mandatory title field. |
| **Google Keep** | Color-coded notes for quick visual scanning. | Optional note accent color (6 warm presets). |
| **Simplenote** | Speed. Markdown toggle, instant sync, no bloat. Tag-based organization. | Tags over folders. Markdown preview toggle. |
| **Bear** | Beautiful typography (Avenir Next). Inline markdown rendering. | Inline formatting preview in editor. Source Serif 4 for content. |

**Common thread**: all four apps get out of the way. No onboarding wizard, no setup, no sign-in gate. The first screen is always the note list, and creating a note is one tap.

## 5. Offline-First & Sync UI

- **Default state**: No sync indicator visible. Silence means success.
- **Syncing**: Subtle animated icon in the top bar (rotating `arrow.triangle.2.circlepath` / M3 circular progress, 16dp). Disappears after completion.
- **Sync error**: Non-blocking snackbar/toast: "Sync failed -- will retry" with a "Retry Now" action. Auto-dismiss 4s.
- **Offline mode**: Thin banner below the app bar: "You're offline -- changes will sync when reconnected". Muted warm-amber background.
- **Conflict resolution**: Toast: "This note was edited on another device -- tap to review". Tapping opens a simple diff with "Keep This / Keep Other / Keep Both" options. Keep it rare by using last-write-wins for non-overlapping edits.

## 6. Typography

| Role | Typeface | Weight | Size (sp/pt) |
|---|---|---|---|
| Note title (editor) | Source Serif 4 | SemiBold (600) | 24 |
| Note body (editor) | Source Serif 4 | Regular (400) | 17 |
| Note title (list) | System (Roboto/SF Pro) | Medium (500) | 16 |
| Note preview (list) | System | Regular (400) | 14 |
| UI labels/buttons | System | Medium (500) | 14 |
| Timestamps | System | Regular (400) | 12, secondary color |

- Source Serif 4: variable font, bundle the `wght` axis (400-700). Excellent readability at body sizes, warm serifs complement the paper-like theme.
- Fallback: Noto Serif or platform serif if Source Serif 4 unavailable.
- Line height for note body: 1.6x for comfortable reading.

## 7. Color Palette

**Light Theme (Paper)**
| Token | Hex | Usage |
|---|---|---|
| `background` | `#FAF6F1` | Main background (warm cream) |
| `surface` | `#FFFFFF` | Cards, sheets, editor |
| `surfaceVariant` | `#F0EBE4` | Search bar, secondary surfaces |
| `onBackground` | `#1C1B18` | Primary text (warm black) |
| `onBackgroundVariant` | `#4A4740` | Secondary text, timestamps |
| `primary` | `#6B5C4D` | Accent -- warm brown |
| `primaryContainer` | `#F5DFC5` | FAB, chips, selection highlight |
| `error` | `#BA1A1A` | Destructive actions |
| `offlineBanner` | `#FFF0D1` | Offline warning background |

**Dark Theme (Warm Dark)**
| Token | Hex | Usage |
|---|---|---|
| `background` | `#1C1B18` | Main background (warm charcoal) |
| `surface` | `#262420` | Cards, sheets, editor |
| `surfaceVariant` | `#33302B` | Search bar, secondary surfaces |
| `onBackground` | `#E8E2D9` | Primary text (warm off-white) |
| `onBackgroundVariant` | `#9C9689` | Secondary text, timestamps |
| `primary` | `#D4B896` | Accent -- warm tan |
| `primaryContainer` | `#4A3F32` | FAB, chips, selection highlight |
| `error` | `#FFB4AB` | Destructive actions |
| `offlineBanner` | `#3D3520` | Offline warning background |

Contrast ratios: all text pairs meet WCAG AA (4.5:1 for body, 3:1 for large text). Verify with the actual bundled font rendering.

## Key Recommendations Summary

1. **Ship the list, not the grid.** A simple list is faster to scan, easier to implement, and matches user mental models for text notes.
2. **Auto-save always.** No save button, no "unsaved changes" dialog. Debounce at 1s.
3. **Platform-native navigation.** FAB + back arrow on Android. Toolbar `+` + back chevron on iOS. Do not force either pattern cross-platform.
4. **Silence means success.** Only show sync UI when something is wrong or actively in progress.
5. **Source Serif 4 for content, system font for chrome.** Gives notes a distinctive, readable feel without fighting platform conventions.
6. **Warm palette, not sterile white.** `#FAF6F1` light / `#1C1B18` dark creates a paper-like warmth that reduces eye strain and differentiates from stock apps.
