<div align="center">

# SnapPurge

**A privacy-first Android app that makes screenshots easy to find and organize.**

[![Version](https://img.shields.io/badge/version-0.4.1-6750A4?style=flat-square)](CHANGELOG.md)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Privacy](https://img.shields.io/badge/privacy-local--only-2563EB?style=flat-square&logo=shield&logoColor=white)
[![GitHub last commit](https://img.shields.io/github/last-commit/ninjin0802/SnapPurge?style=flat-square)](https://github.com/ninjin0802/SnapPurge/commits/main)

[日本語](README.md) · **English**

</div>

## What is SnapPurge?

SnapPurge uses on-device OCR to make growing screenshot libraries searchable, classifiable, and easier to clean up. Images and analysis results remain on the device and are not sent to a developer-operated server.

## Features

- On-device OCR and full-text search
- Automatic categories: Shopping, Schedule, Work, Study, Social, Travel, Finance, and Notes
- Category folders under `Pictures/SnapPurge/<category>`
- Local summaries, tag suggestions, date candidates, and reminders
- Similar and duplicate screenshot detection
- Individual, batch, and delete-all flows
- A clear choice between removing an index entry and deleting the original image
- System, Light, and Dark themes
- Screenshot analysis remains local; networking is limited to user-initiated GitHub update checks
- GitHub release checks with signing-certificate and SHA-256 verification

## Requirements

| Item | Requirement |
|---|---|
| Android | Android 8.0 (API 26) or later |
| Current version | 0.4.1 (versionCode 6) |
| Distribution | GitHub Releases |
| Data processing | On device only |

## Installation

Official APKs are published on [GitHub Releases](https://github.com/ninjin0802/SnapPurge/releases).

1. Download the latest signed APK from Releases.
2. Allow installation from the download source when Android asks.
3. Open the APK and install it.
4. Choose the desired photo access scope on first launch.

> Avoid APKs redistributed elsewhere. Verify the APK against the SHA-256 checksum shown in its GitHub Release.

## Build from source

Open the repository in Android Studio and configure JDK 17 or later with Android SDK Platform 37.0.

```powershell
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Privacy and safety

- Screenshots, OCR text, search terms, and classifications are not uploaded.
- GitHub is contacted only after the user taps the update button; there are no background update checks.
- Original-image moves and deletions use Android system confirmation when required.
- Removing an item from SnapPurge does not delete the original image.
- OFUSE and X are external services governed by their respective policies.

[Privacy Policy](PRIVACY_POLICY.md) · [Terms of Use](TERMS.md)

## Versioning

SnapPurge follows semantic versioning: `MAJOR.MINOR.PATCH`.

- `MAJOR`: incompatible changes
- `MINOR`: backward-compatible features
- `PATCH`: backward-compatible fixes

Every app update increments `versionCode` and keeps `versionName`, the in-app Developer page, the README badge, and `CHANGELOG.md` synchronized. See [VERSIONING.md](VERSIONING.md).

## Releases

Before publishing to GitHub Releases, the project verifies signing, real-device behavior, Lint, tests, checksums, and policy consistency.

[Changelog](CHANGELOG.md) · [Release Checklist](RELEASE_CHECKLIST.md)

## Developer

Developed by **ninjin**.

- X: [@_nin82](https://x.com/_nin82)
- OFUSE: [ofuse.me/ninjin](https://ofuse.me/ninjin)

If SnapPurge is useful to you, support through OFUSE helps fund device testing, quality improvements, and continued development.

## Documentation

- [Development plan](PLAN.md)
- [UI refinement log](UI_REFINEMENT.md)
- [Versioning rules](VERSIONING.md)
- [Changelog](CHANGELOG.md)
- [Privacy policy](PRIVACY_POLICY.md)
- [Terms of use](TERMS.md)
- [Release checklist](RELEASE_CHECKLIST.md)
