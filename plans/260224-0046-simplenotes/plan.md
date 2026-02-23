---
app: SimpleNotes
framework: KMP (Compose Multiplatform)
platforms: [android, ios]
backend: Supabase (gqedjehqxijaxsrqmduk)
architecture: MVI + Clean Architecture
status: pending-approval
created: 2026-02-24
---

# SimpleNotes — Implementation Plan

## Overview
Offline-first note-taking app with search and Supabase cloud sync. KMP shared codebase, Compose Multiplatform UI, SQLDelight local storage, supabase-kt backend.

## Parallelization Strategy
- **Group A** (parallel): Phase 01 (scaffold), Phase 02 (DB/API), Phase 04 (design system)
- **Group B** (parallel, after A): Phase 03 (domain layer), Phase 05 (screens), Phase 06 (platform)
- **Group C** (sequential, after B): Phase 07 (testing) → Phase 08 (store assets) → Phase 09 (build/deploy)

## Screens (4 total)
1. **Auth** — Sign in / Sign up (email+password)
2. **Note List** — Search bar + note list + FAB/toolbar new-note button
3. **Note Editor** — Title + body, auto-save, formatting toolbar
4. **Settings** — Account info, sign out, sync status

## Tech Stack
| Layer | Choice |
|-------|--------|
| UI | Compose Multiplatform (shared) |
| Architecture | Manual MVI (State/Intent/ViewModel) |
| Local DB | SQLDelight + FTS5 |
| Networking | Ktor Client + supabase-kt 3.x |
| Auth | Supabase Auth (email/password) |
| Sync | Offline-first queue + LWW merge |
| DI | Koin |
| Landing Page | Vercel (simple-note.vercel.app) |

## Phase Files
See `phase-01` through `phase-09` in this directory for detailed specs.
