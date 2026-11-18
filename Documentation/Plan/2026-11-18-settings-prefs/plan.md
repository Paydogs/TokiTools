# Theme, Decimal Limit, Auto-shrink Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Theme + decimal-limit settings persisted in SharedPreferences; Proportion honours the limit and shrinks long numbers.

**Architecture:** `settings/UserSettings.kt` (`ThemeMode`, `UserSettings`, `SettingsStore`); `MainActivity` owns store + Compose state, resolves dark mode, styles system bars, passes `settings`/`onSettingsChange` into `TokiToolsApp` → `SettingsScreen` / `ProportionScreen(decimals)`. `Proportion` gains `decimals` handling.

**Tech Stack:** Kotlin, SharedPreferences (core-ktx `edit`), Compose material3 1.4.0, `rememberTextMeasurer`, JUnit4, Compose UI test v2.

**Spec:** `Documentation/Plan/2026-11-18-settings-prefs/design.md`

## Global Constraints

- No new dependencies. Strings in `values/` + `values-hu/`: `theme` Theme/Téma, `theme_system` System/Rendszer, `theme_light` Light/Világos, `theme_dark` Dark/Sötét, `decimals` Decimal places/Tizedesjegyek, `decimals_less` Fewer decimal places/Kevesebb tizedesjegy, `decimals_more` More decimal places/Több tizedesjegy.
- Decimals 0..8, default 4. Prefs file `settings`, keys `theme`, `decimals`.
- Phone: one scripted session (RED snapshots → GREEN → separate install → package, app locales and prefs unchanged). No adb input. The user commits.

## Review Focus

1. Tests must restore the user's saved theme/decimals on the phone.
2. Lowering N with longer existing inputs: deleting still works; results re-round.
3. Dark/Light override vs status-bar icon contrast.
4. Shrink never drops below 16sp; short numbers stay 32sp.
5. Settings → Proportion: new N applied on return (state restored by the holder).

---

### Task 1: Proportion decimals (JVM)

**Files:** `proportion/Proportion.kt`; Test `ProportionTest.kt`

**Interfaces:** `fun edit(field: ProportionField, text: String, decimalSeparator: Char, decimals: Int): Proportion`; `fun recalculated(decimalSeparator: Char, decimals: Int): Proportion`.

- [ ] **Step 1: Tests** (existing helper gains `decimals = 4`): `roundsToGivenDecimals` (3,1,1 → `"0.33"` at 2, `"0"` at 0); `typingBeyondLimit_isRejected` (2: `"1.23"` then `"1.234"` → `"1.23"`); `deletingBeyondLimit_isAllowed` (`"1.2345"` at 4, then `"1.234"` at 2 → `"1.234"`); `zeroDecimals_rejectsSeparator` (`"3"` then `"3,"` at 0 → `"3"`); `recalculated_appliesNewDecimals` (`"0.3333"` → `"0.33"`); `recalculated_appliesNewSeparator` (→ `"0,3333"`).
- [ ] **Step 2: RED** `./gradlew testDebugUnitTest`; **Step 3: implement**; **Step 4: GREEN**.

### Task 2: Settings store, theme, decimals UI, auto-shrink

**Files:** Create `settings/UserSettings.kt`; modify `MainActivity.kt`, `ui/TokiToolsApp.kt`, `ui/SettingsScreen.kt`, `ui/ProportionScreen.kt`, strings. Tests: create `SettingsPrefsRule.kt`, `PreferencesTest.kt`.

**Interfaces:**
- `enum class ThemeMode { System, Light, Dark }`; `data class UserSettings(val theme: ThemeMode = ThemeMode.System, val decimals: Int = DEFAULT_DECIMALS)` with `MIN_DECIMALS = 0`, `MAX_DECIMALS = 8`, `DEFAULT_DECIMALS = 4`; `class SettingsStore(context: Context) { fun load(): UserSettings; fun save(settings: UserSettings) }`, `SettingsStore.PREFS_NAME = "settings"`.
- `TokiToolsApp(language, onLanguageSelect, settings: UserSettings, onSettingsChange: (UserSettings) -> Unit)`; `SettingsScreen(language, onLanguageSelect, settings, onSettingsChange, modifier)`; `ProportionScreen(decimals: Int, modifier)`.
- Test util `SettingsPrefsRule : TestWatcher` — starting: snapshot `prefs.all`, clear; finished: restore.

- [ ] **Step 1: Tests** (`PreferencesTest`, rules: `AppLanguageRule("hu")` order 0, `SettingsPrefsRule()` order 1, compose order 2):
  - `theme_choiceSurvivesRecreation` — Settings, click `"Sötét"` → selected; `recreate()` → still selected.
  - `decimals_stepperBounds` — value tag `"decimals-value"` shows `"4"`; − ×4 → `"0"`, − disabled; + ×8 → `"8"`, + disabled.
  - `decimals_applyToProportion` — − ×2 (→2), back, type 3,1,1 → Y2 `"0,33"`.
  - `decimals_limitTyping` — type `1,23456` char by char → X1 `"1,2345"`.
  - `longNumber_shrinksFont` — X1 `123456789012` → layout font size < 32.sp, ≥ 16.sp; `shortNumber_keepsFullSize` — `"7"` → 32.sp.
- [ ] **Step 2:** strings + `UserSettings.kt` API skeleton so tests compile; snapshot `red-tree-2`.
- [ ] **Step 3: Implement** store, activity wiring (`DisposableEffect(dark) { enableEdgeToEdge(SystemBarStyle.auto(…) { dark }, …) }`), shared radio rows, stepper (`−`/`+` buttons with content descriptions, disabled at bounds), `ProportionScreen` `LaunchedEffect(separator, decimals) { recalculated }`, shrink via `BoxWithConstraints` + `rememberTextMeasurer` (32sp → 16sp, 2sp steps).
- [ ] **Step 4: Mac verify** `assembleDebug assembleDebugAndroidTest lintDebug testDebugUnitTest` → pass, lint 0.

### Task 3: Phone session
- [ ] RED `red-tree` (proportion plan tests) and `red-tree-2` (`PreferencesTest`) → FAIL; GREEN full suite; `installDebug` separately; package present; app locales and prefs unchanged; screenshots of light/dark need user (no adb input).
