# Phase 01: Project Scaffold

## Goal
Initialize KMP project with all dependencies, build config, and CI skeleton.

## Steps

### 1. Init KMP project
- Use `kmp.jetbrains.com` wizard output or manual scaffold
- Structure:
  ```
  simple-notes/
    composeApp/
      src/commonMain/    # Shared UI + logic
      src/androidMain/   # Android SQLite driver, platform DI
      src/iosMain/       # Native SQLite driver, platform DI
    iosApp/              # SwiftUI wrapper
    supabase/            # Migrations dir
    gradle/libs.versions.toml
    build.gradle.kts
    settings.gradle.kts
  ```

### 2. Version catalog (libs.versions.toml)
- Kotlin 2.1.10, Compose Multiplatform 1.7.3, SQLDelight 2.0.2
- Ktor 3.1.0, supabase-kt 3.1.1, Koin 4.0.2
- Kotlinx: serialization 1.7.3, coroutines 1.10.1, datetime 0.6.2
- lifecycle-viewmodel-compose for shared ViewModels

### 3. Gradle config
- Root: kotlin-multiplatform, compose-multiplatform, compose-compiler, sqldelight, kotlin-serialization plugins
- composeApp module: targets android + iosArm64 + iosSimulatorArm64
- SQLDelight config: `SimpleNotesDb` database, `com.simplenotes.app.db` package

### 4. BuildConfig
- Create `BuildConfig.kt` in commonMain with Supabase URL + anon key (injected at build time, not hardcoded)
- `.env` file (gitignored) for local dev

### 5. Git init
- `git init`, `.gitignore` (standard KMP + .env + keystore)
- Initial commit

## Output Files
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`
- `composeApp/build.gradle.kts`
- `composeApp/src/commonMain/kotlin/com/simplenotes/app/App.kt` (entry point)
- `.gitignore`, `.env.example`
