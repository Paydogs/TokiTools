# Proportion history — design

## Intent
Save complete proportions and bring them back with one tap.

## Decisions
- "Megjegyez"/"Remember" under "Törlés"/"Clear"; enabled when all four fields have values (a result was calculated).
- List shows `X1/X2 = Y1/Y2`, newest first, scrolls in the free space; same values again move to the top; max 50.
- Tapping an item fills all four fields and marks the same field as calculated.
- "Lista törlése"/"Clear list" pinned at the bottom, disabled when empty, no confirmation.
- Stored as JSON in the `settings` SharedPreferences (key `proportion_history`); `MainActivity` owns it and passes it down, like the settings.
- Testing paused at the user's request: verified by build + lint only.
