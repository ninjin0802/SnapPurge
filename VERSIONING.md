# SnapPurge versioning rule

## Mandatory rule

Every application update must change and record the version before an APK is distributed.

1. Increment `versionCode` in `app/build.gradle`. It must always increase.
2. Update `versionName` using semantic versioning: `MAJOR.MINOR.PATCH`.
3. Add the release and its user-visible changes to `CHANGELOG.md`.
4. Ensure the version is visible inside the app on the Developer page.
5. Update the version badges and version tables in `README.md` and `README_EN.md`.
6. Build and test the exact commit associated with the Git tag `v<versionName>`.
7. Do not replace an existing GitHub Release asset with different binary contents under the same version.

Current version: **0.5.0** (`versionCode 7`)

The in-app Developer page reads `versionName` from the installed package, so the displayed version and APK metadata use the same source of truth.

Documentation-only changes do not increment the application version. Record them under the current version in `CHANGELOG.md`. Any distributed APK change, including dependency or build configuration changes, requires a new `versionCode` and `versionName` review.
