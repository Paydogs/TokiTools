# Drawer header/footer and Settings — design

## Intent
Give the side panel a "Tools" heading and a footer (version + settings button), and add an empty Settings screen.

## Goals / non-goals
- Goal: "Tools" heading; footer with divider, small version on the left, ⚙ on the right.
- Goal: ⚙ closes the panel and opens Settings (title only).
- Goal: Settings is a sub-page: ← replaces ☰; ← or Back returns to the tool you came from.
- Non-goal: any settings content; navigation library.

## Approaches
- **Chosen — `settingsOpen` flag over the current tool:** returning is free; the tool list stays tools-only.
- Settings as a third enum entry + "previous screen": more state; drawer must filter it out.
- Navigation library: overkill for one sub-page.

## Decisions
- Rename `Screen` → `Tool`; add `rememberSaveable` `settingsOpen`.
- Version from `BuildConfig.VERSION_NAME`, shown as `v1.0`, small grey text.
- Drawer gestures disabled on Settings (sub-pages have no drawer).
- Strings with Hungarian: Tools/Eszközök, Settings/Beállítások, Back/Vissza.
- Verified by instrumented Compose UI tests on the phone.

## Left to the builder
Heading style/padding, footer padding.
