# SnapPurge

SnapPurge is a privacy-first Android app for organizing screenshots locally.

Current version: **0.2.0**

The app scans screenshots from the device, runs on-device OCR, classifies them with deterministic local rules, builds a local search index, extracts deadline candidates, detects near-duplicates, and lets the user organize everything with a modern Material 3 interface.

## Privacy stance

- No `INTERNET` permission is declared.
- Screenshot images and OCR text remain on the device.
- Search, summaries, categories, reminders, and duplicate detection are implemented locally.
- The app uses Android media permissions only to read images the user allows.

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- Room + SQLite FTS
- DataStore
- WorkManager
- ML Kit on-device text recognition
- Coil for local image URI rendering

## Open in Android Studio

Open this directory:

```text
C:\Users\meita\project\snapshelf
```

Then sync Gradle and run the `app` configuration on an Android device or emulator.

## Current implementation status

This repository contains the first full implementation pass: app foundation, local data model, local scanner/OCR pipeline, search index, local classifiers/summaries/date extraction, reminder worker, duplicate hashing, and Compose UI with system/light/dark theme switching.

## Developer and support

- Developer: **ninjin**
- Support development through [OFUSE](https://ofuse.me/ninjin)
- Follow [@_nin82 on X](https://x.com/_nin82)

If SnapPurge is useful to you, an OFUSE message or donation helps sustain maintenance, testing, and future improvements.

## Distribution and policies

SnapPurge is intended for distribution through GitHub Releases, not Google Play.

- [Versioning rule](VERSIONING.md)
- [Changelog](CHANGELOG.md)
- [Privacy policy](PRIVACY_POLICY.md)
- [Terms of use](TERMS.md)
- [Release checklist](RELEASE_CHECKLIST.md)
