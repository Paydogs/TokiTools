# Settings Fix + Language Selector Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the drawer-restored-open-on-Settings trap, then add a Magyar/English selector on Settings with Hungarian as default.

**Architecture:** Task 1 adjusts `TokiToolsApp` drawer gating. Task 2 adds `AppLanguage` (wraps `LocaleManager`), a `SettingsScreen` composable, a startup default in `MainActivity`, and an `AppLanguageRule` for tests.

**Tech Stack:** Kotlin, Compose material3 1.4.0, `android.app.LocaleManager` (API 33), AGP 9.5.1 `generateLocaleConfig`, Compose UI test v2.

**Spec:** `Documentation/Plan/2026-11-18-language/design.md` (Task 1: final-review finding in `.superpowers/sdd/plan-2026-11-18-settings/progress.md`)

## Global Constraints

- minSdk 33 (from 31); compile/targetSdk 37 unchanged. No new dependencies.
- Language tags `hu`, `en`; native names `Magyar`, `English` (not translated). New string `language` Language/Nyelv.
- Phone time is scarce: write all tests first; implement production code in a scratch copy (`rsync` of the project) and compile + lint + unit-test it there; then one phone session runs RED on the real tree, copies the implementation in, runs GREEN.
- Never inject taps/swipes via adb while the user might be using the phone; instrumented runs only after the user hands it over.
- `installDebug` never in the same Gradle call as `connectedDebugAndroidTest`; after any run verify `adb shell pm list packages | grep hu.paydogs.tokitools`.
- The user commits; no `git commit` steps.

## Review Focus

1. Tests must not overwrite the user's own language choice on the phone (rule restores previous app locales).
2. Language switch recreates the activity: Settings must stay open, drawer closed, tool kept.
3. First launch recreation (default applied) must not break test sync or crash.
4. Phone settings per-app language list shows exactly Hungarian and English (check `locale_config` generated in the merged manifest).
5. Drawer restored open on Settings: scrim tap and tool tap both recover (Task 1).

---

### Task 1: Drawer restored open on Settings can be dismissed

**Files:**
- Modify: `app/src/main/java/hu/paydogs/tokitools/ui/TokiToolsApp.kt` (`gesturesEnabled`, `onToolClick`)
- Test: `SettingsTest.kt` — already written: `toolTap_whenDrawerRestoredOpenOnSettings_showsTool`, `scrimTap_whenDrawerRestoredOpenOnSettings_closesDrawer`

- [ ] **Step 1 (phone session): RED** — both tests FAIL on current code (tool tap: title stays Settings; scrim tap: drawer stays open).
- [ ] **Step 2: Implement** — `gesturesEnabled = !settingsOpen || drawerState.isOpen`; `onToolClick` also sets `settingsOpen = false`.
- [ ] **Step 3 (phone session): mutation check** — with `gesturesEnabled = true`, `swipe_onSettings_doesNotOpenDrawer` FAILS (drawer item displayed); restore.
- [ ] **Step 4 (phone session): GREEN** — covered by Task 2 Step 5 full run.

### Task 2: Language selector, Hungarian default

**Files:**
- Create: `app/src/main/java/hu/paydogs/tokitools/AppLanguage.kt`
- Create: `app/src/main/java/hu/paydogs/tokitools/ui/SettingsScreen.kt`
- Create: `app/src/main/res/resources.properties` (`unqualifiedResLocale=en-US`)
- Modify: `app/build.gradle.kts` (minSdk 33; `androidResources { generateLocaleConfig = true }`), `MainActivity.kt`, `ui/TokiToolsApp.kt` (Settings body), strings (`language`)
- Test: create `app/src/androidTest/.../AppLanguageRule.kt`, `LanguageTest.kt`, `DefaultLanguageTest.kt`; modify `SideNavigationTest`, `SettingsTest` to add `@get:Rule(order = 0) val language = AppLanguageRule("hu")` and `order = 1` on the compose rule.

**Interfaces:**
- Produces: `enum class Language(val tag: String, val nativeName: String) { Hungarian("hu", "Magyar"), English("en", "English") }` and `class AppLanguage(context: Context)` with `val current: Language` (empty locales → Hungarian), `fun set(language: Language)`, `fun applyDefault()` (sets Hungarian only when `applicationLocales.isEmpty`) — in `AppLanguage.kt`
- Produces: `@Composable fun SettingsScreen(selected: Language, onSelect: (Language) -> Unit, modifier: Modifier = Modifier)` — heading `R.string.language`, one `selectable(role = Role.RadioButton)` row per `Language.entries` inside `selectableGroup()`
- Test util: `class AppLanguageRule(private val tag: String?) : TestWatcher` — `starting`: save current app locales, set `tag` (null → empty); `finished`: restore saved

- [ ] **Step 1: Write tests** (literals are the behaviour under test here)
  - `DefaultLanguageTest` (rule `null`): `firstLaunch_withoutAppLanguage_showsHungarian` — title == `"Arány"`; target-context `LocaleManager.applicationLocales.toLanguageTags() == "hu"`
  - `LanguageTest` (rule `"hu"`), helper `openSettings()` as in `SettingsTest`:
    - `settings_marksCurrentLanguage` — `onNodeWithText("Magyar").assertIsSelected()`, `"English"` not selected
    - `choosingEnglish_switchesAndStaysOnSettings` — click `"English"`; title == `"Settings"`; `"English"` selected; locales == `"en"`
    - `choosingMagyar_afterEnglish_switchesBack` — click `"English"`, then `"Magyar"`; title == `"Beállítások"`; locales == `"hu"`
- [ ] **Step 2: Compile tests on the real tree** — `./gradlew assembleDebugAndroidTest` → BUILD SUCCESSFUL (tests reference no new production symbols).
- [ ] **Step 3: Implement in a scratch copy**, then `./gradlew assembleDebug assembleDebugAndroidTest lintDebug testDebugUnitTest` there → BUILD SUCCESSFUL, lint 0; merged manifest has `android:localeConfig`.
- [ ] **Step 4 (phone session): RED on real tree** — `connectedDebugAndroidTest` for `LanguageTest`, `DefaultLanguageTest`, Task 1 tests → all FAIL (no "Magyar" node / title "Proportion" / Task 1 reasons).
- [ ] **Step 5 (phone session): GREEN** — copy implementation files in; Task 1 Step 3 mutation run; `./gradlew connectedDebugAndroidTest` → 19 tests, 0 failures; then separately `./gradlew installDebug`; `pm list packages` shows the app; `adb shell cmd locale get-app-locales hu.paydogs.tokitools` shows the user's prior choice (or `hu`).
- [ ] **Step 6: lint/unit on real tree** — `./gradlew testDebugUnitTest lintDebug` → 0 issues.
