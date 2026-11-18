# Drawer Header/Footer and Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** "Tools" heading and a version + ⚙ footer in the drawer; ⚙ opens an empty Settings sub-page that ← / Back leave.

**Architecture:** `TokiToolsApp` keeps the current `Tool` plus a `settingsOpen` flag (both `rememberSaveable`). Drawer content moves to its own composable. Settings swaps ☰ for ←, disables drawer gestures, and installs a `BackHandler`.

**Tech Stack:** Kotlin, Compose (material3 1.4.0, material-icons-core 1.7.8: `Icons.Filled.Settings`, `Icons.AutoMirrored.Filled.ArrowBack`), activity-compose `BackHandler`, Compose UI test v2.

**Spec:** `Documentation/Plan/2026-11-18-settings/design.md`

## Global Constraints

- No navigation library. minSdk 31, compile/targetSdk 37 unchanged.
- Every translatable string in `values/` and `values-hu/`: `tools` Tools/Eszközök, `settings` Settings/Beállítások, `back` Back/Vissza.
- Version text `v` + `BuildConfig.VERSION_NAME` (currently `v1.0`), via a `translatable="false"` resource `version` = `v%1$s`.
- Tests read expected text from resources / `BuildConfig`, never English literals.
- CLI Gradle needs `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`; instrumented runs need the phone unlocked.
- The user commits; no `git commit` steps.

## Review Focus

1. Back on Settings returns to the tool, not exits; Back on a tool still exits (unchanged).
2. Rotation on Settings stays on Settings; ← afterwards still returns to the same tool.
3. Swipe on Settings must not open the drawer.
4. Footer stays at the bottom and above the gesture bar (sheet insets), version readable at small size.
5. Hungarian: all new strings translated (lint `MissingTranslation`).

---

### Task 1: Drawer heading, footer and Settings sub-page

**Files:**
- Rename: `app/src/main/java/hu/paydogs/tokitools/ui/Screen.kt` → `ui/Tool.kt` (`enum class Tool`, same entries)
- Create: `app/src/main/java/hu/paydogs/tokitools/ui/ToolsDrawerSheet.kt`
- Modify: `app/src/main/java/hu/paydogs/tokitools/ui/TokiToolsApp.kt`
- Modify: `app/build.gradle.kts` (`buildFeatures { buildConfig = true }`)
- Modify: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-hu/strings.xml`
- Test: `app/src/androidTest/java/hu/paydogs/tokitools/SideNavigationTest.kt` (existing tests keep passing), new `SettingsTest.kt`

**Interfaces:**
- Produces: `enum class Tool(@StringRes val title: Int) { Proportion, Percentage }`
- Produces: `@Composable fun ToolsDrawerSheet(drawerState: DrawerState, selected: Tool, onToolClick: (Tool) -> Unit, onSettingsClick: () -> Unit)` — wraps `ModalDrawerSheet(drawerState = drawerState)`
- Keeps: `SCREEN_TITLE_TAG`, `TokiToolsApp()`

- [ ] **Step 1: Write the failing tests** — `SettingsTest` (same helpers as `SideNavigationTest`: `str`, `title()`, `item`, `openDrawer`; plus `openSettings()` = openDrawer + `onNodeWithContentDescription(str(R.string.settings)).performClick()`, `upButton()` = `onNodeWithContentDescription(str(R.string.back))`).
  - `drawer_showsToolsHeadingAndVersion`: openDrawer; `onNodeWithText(str(R.string.tools)).assertIsDisplayed()`; `onNodeWithText("v" + BuildConfig.VERSION_NAME).assertIsDisplayed()`
  - `settingsButton_closesDrawerAndShowsSettings`: openSettings; title == `str(R.string.settings)`; `item(R.string.proportion).assertIsNotDisplayed()`; `upButton().assertIsDisplayed()`
  - `back_onSettings_returnsToPreviousTool`: switch to Percentage; openSettings; `Espresso.pressBack()`; title == percentage
  - `upButton_onSettings_returnsToPreviousTool`: switch to Percentage; openSettings; `upButton().performClick()`; title == percentage; `onNodeWithContentDescription(str(R.string.open_menu)).assertIsDisplayed()`
  - `settings_survivesRecreation`: switch to Percentage; openSettings; `rule.activityRule.scenario.recreate()`; title == settings; `upButton().performClick()`; title == percentage
  - `swipe_onSettings_doesNotOpenDrawer`: openSettings; `rule.onRoot().performTouchInput { swipeRight(startX = centerX / 2, endX = right) }`; `item(R.string.proportion).assertIsNotDisplayed()`

- [ ] **Step 2: Run to verify RED** — add strings, `buildConfig = true` and the `Screen`→`Tool` rename first so tests compile.

  Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=hu.paydogs.tokitools.SettingsTest`
  Expected: 6 FAIL — no "Tools"/version/settings/back nodes. `SideNavigationTest` unaffected.

- [ ] **Step 3: Implement**
  - `ToolsDrawerSheet`: `Column` heading `Text(tools, titleLarge)` → tool items (as today, `ItemPadding`) → `Spacer(Modifier.weight(1f))` → `HorizontalDivider` → `Row(verticalAlignment = CenterVertically)`: `Text(stringResource(R.string.version, BuildConfig.VERSION_NAME), labelSmall, color = onSurfaceVariant)`, `Spacer(weight 1f)`, `IconButton(onSettingsClick) { Icon(Icons.Filled.Settings, contentDescription = settings) }`.
  - `TokiToolsApp`: `tool`, `settingsOpen` saveable; `ModalNavigationDrawer(gesturesEnabled = !settingsOpen)`; onToolClick sets tool, `settingsOpen = false`, closes drawer; onSettingsClick sets `settingsOpen = true`, closes drawer; title = settings or tool title; navigationIcon = ← (`Icons.AutoMirrored.Filled.ArrowBack`, `back`) clearing `settingsOpen`, else ☰; `BackHandler(enabled = settingsOpen) { settingsOpen = false }`.

- [ ] **Step 4: Run GREEN**

  Run: `./gradlew connectedDebugAndroidTest`
  Expected: 13 tests (6 new + 7 existing), 0 failures.

- [ ] **Step 5: Full check and deploy**

  Run: `./gradlew testDebugUnitTest lintDebug installDebug`
  Expected: BUILD SUCCESSFUL; lint 0 issues.

- [ ] **Step 6: Manual check on the phone** — screenshot drawer (heading, footer at bottom above gesture bar), Settings screen; Back/← return; Review Focus 4.
