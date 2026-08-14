# GitHub Releases checklist

SnapPurge is distributed through GitHub Releases, not Google Play.

## Required before every release

- [ ] Follow `VERSIONING.md`; increment `versionCode` and update `versionName`
- [ ] Update `CHANGELOG.md`
- [ ] Confirm the Developer page displays the new version
- [ ] Run `testDebugUnitTest`, `lintDebug`, and `assembleRelease`
- [ ] Test install/update, screenshot access, local OCR, folder moves, and guarded deletion on a real device
- [ ] Confirm the merged manifest has no `INTERNET` or `ACCESS_NETWORK_STATE` permission
- [ ] Build a release APK signed with the stable ninjin release key; never distribute a debug APK as a release
- [ ] Generate and publish the APK SHA-256 checksum
- [ ] Create annotated Git tag `v<versionName>` from the tested commit
- [ ] Attach the signed APK and checksum to the matching GitHub Release
- [ ] Include changes, minimum Android version, installation steps, permissions, and known issues in release notes
- [ ] Ensure `PRIVACY_POLICY.md`, `TERMS.md`, and source code match the distributed binary

## GitHub repository setup still required

- Create or select the public GitHub repository
- Add a license file chosen by ninjin
- Configure a persistent release signing key and store it outside the repository
- Never commit keystores, passwords, tokens, or signing secrets
- Enable GitHub release automation only after signing secrets have been configured safely

An APK is release-ready only after every required item above is complete.
