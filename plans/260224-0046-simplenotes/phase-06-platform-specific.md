# Phase 06: Platform-Specific

## Goal
Handle Android/iOS platform differences and native integrations.

## Android (androidMain)
- SQLite driver: `AndroidSqliteDriver`
- Supabase session storage: `EncryptedSharedPreferences`
- Network connectivity observer: `ConnectivityManager` callback
- Edge-to-edge display, status bar theming
- App icon: adaptive icon (foreground + background layers)

## iOS (iosMain + iosApp)
- SQLite driver: `NativeSqliteDriver`
- Supabase session storage: `NSUserDefaults` (or Keychain for tokens)
- Network connectivity: `NWPathMonitor`
- SwiftUI wrapper: `ComposeUIViewController` in ContentView
- iOS app icon: single 1024x1024 asset
- Info.plist: privacy descriptions if needed

## Shared expect/actual
- `expect fun createSqlDriver(): SqlDriver`
- `expect fun isNetworkAvailable(): Flow<Boolean>`
- `expect fun platformName(): String`

## Output Files
- `androidMain/kotlin/.../Platform.android.kt`
- `iosMain/kotlin/.../Platform.ios.kt`
- `iosApp/iosApp/ContentView.swift`
- `androidApp/src/main/AndroidManifest.xml`
