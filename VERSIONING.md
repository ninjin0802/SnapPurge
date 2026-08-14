# SnapPurge versioning rule

## Mandatory rule

Every application update must change and record the version before an APK is distributed.

1. Increment `versionCode` in `app/build.gradle`. It must always increase.
2. Update `versionName` using semantic versioning: `MAJOR.MINOR.PATCH`.
3. Add the release and its user-visible changes to `CHANGELOG.md`.
4. Ensure the version is visible inside the app on the Developer page.
5. Build and test the exact commit associated with the Git tag `v<versionName>`.
6. Do not replace an existing GitHub Release asset with different binary contents under the same version.

Current version: **0.2.0** (`versionCode 2`)

The in-app Developer page reads `versionName` from the installed package, so the displayed version and APK metadata use the same source of truth.
