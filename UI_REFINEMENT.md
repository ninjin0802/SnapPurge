# SnapPurge UI refinement loop

## Goal

Make the Android UI simpler, clearer, and more visually focused on screenshots while preserving fully local processing.

## Agent roles and models

- UI/UX review: `gpt-5.6-terra`
- Integration, implementation, device verification: parent agent model
- Existing architecture and privacy gates from the Android architect and QA roles remain applicable

## Changes

- Removed the oversized home hero card and repeated per-card action buttons
- Reduced the home hierarchy to title, search, category filters, screenshot grid, and one add FAB
- Moved OCR text behind an expandable action on the detail screen
- Kept theme selection in a compact icon menu with System, Light, and Dark choices
- Added an individual delete flow with separate “remove from SnapPurge” and “delete original from device” choices
- Added batch selection by long press and batch deletion
- Added a guarded delete-all flow with separate index-only and original-file deletion choices
- Added category folder organization under `Pictures/SnapPurge/<category>`
- A completed scan requests Android system write approval, then moves originals into their classified folders
- Device deletion uses Android's system confirmation dialog on Android 11 and newer

## Verification

- Unit tests pass
- Debug APK builds successfully
- Android Lint passes
- Updated APK installed successfully on Pixel 9a
- Destructive deletion was not executed during automated verification
