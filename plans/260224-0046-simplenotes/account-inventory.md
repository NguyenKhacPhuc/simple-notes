# Account Inventory — SimpleNotes

## App Info
- **Name:** SimpleNotes
- **Package:** com.simplenotes.app
- **Platforms:** Android, iOS
- **Framework:** KMP (Kotlin Multiplatform)
- **Features:** notes, search, offline, cloud-sync, auth

## Services Status

| Service | Status | Details |
|---------|--------|---------|
| Supabase | ✅ Provisioned | Project: gqedjehqxijaxsrqmduk, Region: us-east-1 |
| Firebase | ❌ Not enabled | Analytics provider set to "firebase" but project not created |
| Vercel | ✅ Enabled | auto_create, domain: simple-note.vercel.app |
| Railway | ❌ Not needed | No custom backend API required |
| RevenueCat | ❌ Not needed | No payments |

## Auth Providers
- None configured (will use Supabase Auth email/password)

## Store Accounts
- Google Play: Not configured
- Apple Developer: Not configured

## Git
- Remote: https://github.com/NguyenKhacPhuc/simple-notes.git
- Auto-commit: true
- Auto-push: true

## Notes
- Firebase analytics: config says provider "firebase" but firebase project not enabled. Will skip Firebase analytics for MVP — can add later.
- Supabase URL: https://gqedjehqxijaxsrqmduk.supabase.co
- Landing page will be deployed to Vercel at simple-note.vercel.app
