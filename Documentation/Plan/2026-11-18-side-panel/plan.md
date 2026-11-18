# Side Panel Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Left drawer with Proportion and Percentage; tapping one closes the drawer and shows that (placeholder) screen.

**Architecture:** `MainActivity` hosts `TokiToolsApp()`: a Material 3 `ModalNavigationDrawer` around a `Scaffold` whose `CenterAlignedTopAppBar` shows ☰ and the current screen's name. Current screen is a `Screen` enum held in `rememberSaveable`. No navigation library.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2026.09.00, material3 1.4.0), `material-icons-core`, Compose UI test (instrumented).

**Spec:** `Documentation/Plan/2026-11-18-side-panel/design.md`

## Global Constraints

- minSdk 31, compileSdk/targetSdk 37 — unchanged.
- No navigation library.
- Every user-visible string in `res/values/strings.xml` **and** `res/values-hu/strings.xml` (lint `MissingTranslation` fails the build otherwise).
- Copy: `Proportion` / `Arány`, `Percentage` / `Százalék`, menu button description `Open menu` / `Menü megnyitása`.
- Gradle from CLI needs `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- The user commits; no `git commit` steps.

## Review Focus

1. Phone language Hungarian → titles read "Arány"/"Százalék"; tests must read expected text from resources, never English literals.
2. Tapping the already-selected item → drawer closes, screen unchanged.
3. Back with the drawer open → drawer closes, app stays open (needs `ModalDrawerSheet(drawerState = …)`).
4. Rotation / activity recreation → selected screen kept.
5. Gesture navigation: a left-edge swipe may be taken by system Back instead of opening the drawer — manual check on the phone; ☰ must always work.

---

### Task 1: Drawer navigation between Proportion and Percentage

**Files:**
- Create: `app/src/main/java/hu/paydogs/tokitools/ui/Screen.kt`
- Create: `app/src/main/java/hu/paydogs/tokitools/ui/TokiToolsApp.kt`
- Modify: `app/src/main/java/hu/paydogs/tokitools/MainActivity.kt` (replace `Greeting`/`GreetingPreview` with `TokiToolsApp()`)
- Modify: `app/src/main/res/values/strings.xml`, `app/src/main/res/values-hu/strings.xml`
- Modify: `gradle/libs.versions.toml`, `app/build.gradle.kts` (icons dependency)
- Test: `app/src/androidTest/java/hu/paydogs/tokitools/SideNavigationTest.kt`

**Interfaces:**
- Produces: `enum class Screen(@StringRes val title: Int) { Proportion(R.string.proportion), Percentage(R.string.percentage) }` in `ui/Screen.kt`
- Produces: `@Composable fun TokiToolsApp()` and `internal const val SCREEN_TITLE_TAG = "screenTitle"` in `ui/TokiToolsApp.kt`
- String resources: `proportion`, `percentage`, `open_menu`

- [ ] **Step 1: Write the failing test** — `SideNavigationTest` with `createAndroidComposeRule<MainActivity>()`; helpers: `str(id) = rule.activity.getString(id)`, `title() = onNodeWithTag(SCREEN_TITLE_TAG)`, `item(id) = onNode(hasText(str(id)) and hasClickAction())`, `openDrawer() = onNodeWithContentDescription(str(R.string.open_menu)).performClick()`.

  - `startsOnProportion`: `title().assertTextEquals(str(R.string.proportion))`
  - `tappingPercentage_closesDrawerAndShowsPercentage`: openDrawer; `item(R.string.percentage).performClick()`; `title().assertTextEquals(str(R.string.percentage))`; `item(R.string.percentage).assertIsNotDisplayed()`
  - `tappingCurrentItem_closesDrawerAndKeepsScreen`: openDrawer; `item(R.string.proportion).performClick()`; title == proportion; `item(R.string.proportion).assertIsNotDisplayed()`
  - `back_closesOpenDrawer`: openDrawer; `item(R.string.percentage).assertIsDisplayed()`; `Espresso.pressBack()`; `item(R.string.percentage).assertIsNotDisplayed()`; `assertFalse(rule.activity.isFinishing)`
  - `selection_survivesRecreation`: switch to Percentage; `rule.activityRule.scenario.recreate()`; title == percentage

- [ ] **Step 2: Run to verify it fails**

  Run: `./gradlew :app:compileDebugAndroidTestKotlin`
  Expected: FAIL — unresolved `SCREEN_TITLE_TAG`, `R.string.proportion`, …

- [ ] **Step 3: Add strings and icons dependency** — strings per Global Constraints in both files. `libs.versions.toml`: `androidx-compose-material-icons-core = { group = "androidx.compose.material", name = "material-icons-core" }` (version from BOM); `app/build.gradle.kts`: `implementation(libs.androidx.compose.material.icons.core)`.

- [ ] **Step 4: Implement `Screen` and `TokiToolsApp()`**
  - State: `var screen by rememberSaveable { mutableStateOf(Screen.Proportion) }`, `rememberDrawerState(DrawerValue.Closed)`, `rememberCoroutineScope()`.
  - Drawer: `ModalDrawerSheet(drawerState = drawerState)` (gives Back-to-close); one `NavigationDrawerItem` per `Screen.entries`, `selected = entry == screen`, onClick sets `screen` and launches `drawerState.close()`.
  - Top bar: `CenterAlignedTopAppBar`; navigationIcon = `IconButton` with `Icons.Filled.Menu`, contentDescription `R.string.open_menu`, launches `drawerState.open()`; title = `Text(stringResource(screen.title), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.testTag(SCREEN_TITLE_TAG))`.
  - Body: nothing yet (the `when (screen)` arrives with real content). Add a `@Preview` of `TokiToolsApp` inside `TokiToolsTheme`.
  - `MainActivity.setContent { TokiToolsTheme { TokiToolsApp() } }`; delete `Greeting`, `GreetingPreview` and now-unused imports.

- [ ] **Step 5: Run tests on the phone (unlocked)**

  Run: `./gradlew connectedDebugAndroidTest`
  Expected: 6 tests, 0 failures (5 new + `ExampleInstrumentedTest`).

- [ ] **Step 6: Full check and deploy**

  Run: `./gradlew testDebugUnitTest lintDebug installDebug`
  Expected: BUILD SUCCESSFUL; `app/build/reports/lint-results-debug.xml` has 0 issues.

- [ ] **Step 7: Manual check on the phone** — launch shows "Proportion" large and centered; ☰ opens drawer; Percentage switches and closes; Back with drawer open closes it; edge swipe behaviour noted (Review Focus 5); dark mode looks right.
