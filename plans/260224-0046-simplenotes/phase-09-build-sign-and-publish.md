# Phase 09: Build, Sign & Publish

## Goal
Configure release builds. Store publishing deferred (no store accounts configured).

## Android Release Build
- Generate signing keystore: `keytool -genkey -v -keystore release.jks ...`
- `keystore.properties` (gitignored) with passwords
- `build.gradle.kts`: release signing config, R8 minification enabled
- Build: `./gradlew :composeApp:assembleRelease` → APK
- Build: `./gradlew :composeApp:bundleRelease` → AAB (for Play Store)

## iOS Build
- Deferred: no Apple Developer account configured
- Xcode project configured for debug/simulator builds

## Git Final
- Ensure all files committed
- Push to https://github.com/NguyenKhacPhuc/simple-notes.git
- Tag: v1.0.0-beta

## Manual Setup Needed (post-pipeline)
- Google Play Console account → upload AAB
- Apple Developer Program → configure signing → upload IPA
- Firebase project → analytics integration (optional)

## Output
- Signed APK/AAB
- Final git tag + push
- Summary report at `plans/.../reports/`
