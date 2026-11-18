# Theme, decimal limit, auto-shrink — design

## Intent
Settings gets a light/dark choice and a decimal limit; Proportion fields shrink long numbers to fit.

## Goals / non-goals
- Goal: Settings order: Nyelv, Téma (Rendszer/Világos/Sötét, default Rendszer), Tizedesjegyek (− N +, 0–8, default 4).
- Goal: N rounds results and limits typing (deleting always allowed; N = 0 rejects the separator); changing N recalculates.
- Goal: field text 32sp, stepping down to 16sp to fit; hints keep 32sp.
- Non-goal: theming the launch window (follows the phone); DataStore.

## Approaches
- **Chosen — SharedPreferences store owned by `MainActivity`** (user's pick): synchronous, no theme flash, no new library.
- DataStore: async first read needs flash handling; extra library.
- Text-field auto-size: not available in material3 1.4.0; measure with `TextMeasurer` instead.

## Decisions
- `UserSettings(theme, decimals)` + `SettingsStore` (prefs "settings"); activity holds Compose state, saves on change, passes settings down.
- System-bar icon style follows the resolved theme (`enableEdgeToEdge` with `SystemBarStyle.auto { dark }`).
- `Proportion.edit` takes `decimals`; rejects an edit that lengthens the text beyond the limit; `recalculated(separator, decimals)` re-solves on setting/language change.
- Theme uses radio rows; language uses a dropdown (user request, 2026-11-18).
- Tests restore the user's saved settings on the phone.

## Left to the builder
Stepper look, shrink step size.
