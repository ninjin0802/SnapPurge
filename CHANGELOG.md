# Changelog

## 0.4.1 - 2026-08-14

- Fixed crashes when Android rejects a MediaStore delete confirmation request
- Added safe handling for inaccessible, expired, or unsupported image URIs
- Prevented unhandled database deletion errors from terminating the app
- Added clear in-app error messages when an image cannot be deleted

## 0.4.0 - 2026-08-14

- Added an in-app release-notes view before installing an available update
- Added visible security verification results for SHA-256, signing certificate, and package identity
- Restricted updates to exact versioned assets from the official HTTPS GitHub repository
- Added APK size, checksum format, package name, and internal version validation

## 0.3.1 - 2026-08-14

- Redesigned the Developer page with a quieter card-and-list hierarchy
- Made rows with chevrons directly tappable
- Removed redundant “open” and “view details” buttons

## 0.3.0 - 2026-08-14

- Added user-initiated update checks against the official GitHub Releases page
- Added APK download with SHA-256 and ninjin signing-certificate verification
- Added a guarded handoff to the Android package installer
- Updated privacy documentation for update-only network access

## 0.2.0 - 2026-08-14

- Reworked the repository documentation with Japanese and English READMEs and standard status badges
- Renamed the product to SnapPurge
- Simplified the Material 3 screenshot-first interface
- Added System, Light, and Dark theme selection
- Added fully local OCR, classification, summaries, search, and reminders
- Added category folder organization under `Pictures/SnapPurge`
- Added individual, batch, and delete-all flows with Android system confirmation
- Added the Developer page, version display, OFUSE link, X link, privacy policy, and terms

## 0.1.0 - 2026-08-14

- Initial Android development build
