# Language Review Fixes + Proportion Calculator Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the two Important language-review fixes, then build the Proportion calculator.

**Architecture:** `MainActivity` owns the single `AppLanguage` and passes `language`/`onLanguageSelect` into `TokiToolsApp`. Proportion logic is a pure-Kotlin immutable model (`proportion/Proportion.kt`) unit-tested on the JVM; `ProportionScreen` renders it; per-tool state lives under `rememberSaveableStateHolder` keyed by tool.

**Tech Stack:** Kotlin, `BigDecimal`, Compose material3 1.4.0, JUnit4 (JVM), Compose UI test v2.

**Spec:** `Documentation/Plan/2026-11-18-proportion/design.md`; Task 1 from `.superpowers/sdd/plan-2026-11-18-language/progress.md` (final review).

## Global Constraints

- No new dependencies. minSdk 33, compile/targetSdk 37.
- New strings in `values/` and `values-hu/`: `clear` Clear/Törlés.
- Phone: one scripted session after all code is written (RED snapshot → GREEN → separate `installDebug` → `pm list packages` + app locales unchanged). No adb input while the user may use the phone.
- The user commits.

## Review Focus

1. Editing the computed field: oldest input becomes the result, never an input the user just typed.
2. Clearing / invalid input: a stale computed value never lingers as if valid.
3. Hungarian comma vs dot: `1,5` and `1.5` equal; output separator follows the app language.
4. Values survive rotation and Proportion → Percentage → Proportion.
5. Preview renders (no system services in `TokiToolsApp`).

---

### Task 1: Language review fixes

**Files:** `MainActivity.kt`, `ui/TokiToolsApp.kt`, `res/resources.properties`; Test: `LanguageTest.kt`

**Interfaces:** Produces `@Composable fun TokiToolsApp(language: Language, onLanguageSelect: (Language) -> Unit)`; MainActivity: one `AppLanguage(this)` → `applyDefault()`, `current` read once in `onCreate`.

- [ ] **Step 1: Failing test** — `LanguageTest.localeConfig_matchesSelectableLanguages`: `LocaleConfig(targetContext).supportedLocales` tags as a set == `Language.entries.map { it.tag }.toSet()` (`{"en","hu"}`). RED on phone run (currently `en-US`).
- [ ] **Step 2: Fix** — `unqualifiedResLocale=en`; move `AppLanguage` to `MainActivity` per Interfaces; preview passes `Language.Hungarian` and `{}`.
- [ ] **Step 3: Verify** — `assembleDebug lintDebug` on Mac; GREEN in the phone run.

### Task 2: Proportion model (JVM)

**Files:** Create `app/src/main/java/hu/paydogs/tokitools/proportion/Proportion.kt`; Test `app/src/test/java/hu/paydogs/tokitools/proportion/ProportionTest.kt`

**Interfaces:**
- `enum class ProportionField { X1, X2, Y1, Y2 }`
- `data class Proportion(val texts: Map<ProportionField, String>, val typed: List<ProportionField>, val result: ProportionField?)` with `fun edit(field: ProportionField, text: String, decimalSeparator: Char): Proportion`, `fun isInvalid(field: ProportionField): Boolean` (non-blank, not a number), `companion object { val Empty: Proportion }`
- Rules per design; formulas X1 = X2·Y1/Y2, X2 = X1·Y2/Y1, Y1 = X1·Y2/X2, Y2 = X2·Y1/X1; `divide(…, 4, HALF_UP).stripTrailingZeros().toPlainString()` with `.` → separator.

- [ ] **Step 1: Failing tests** (helper `fill(vararg pairs, sep = '.')` editing in order):
  - `solvesEachField` — Y2 from (2,4,3)→`"6"`; X1 from X2=4,Y1=3,Y2=6 →`"2"`; X2 from X1=2,Y1=3,Y2=6 →`"4"`; Y1 from X1=2,X2=4,Y2=6 →`"3"`; `result` names the solved field.
  - `editingResult_makesOldestInputTheResult` — X1=2,X2=4,Y1=3 (Y2=6), then Y2=`"8"` → `result == X1`, X1 `"3"`.
  - `clearingAnInput_emptiesComputedField` — after Y2 computed, X1=`""` → Y2 `""`, `result == null`.
  - `invalidInput_noResultAndFlagged` — X1=`"abc"` → `isInvalid(X1)`, no result.
  - `zeroDenominator_noResult` — X2=`"0"`,X1=2,Y1=3 → Y2 `""`; X1=`"0"`,X2=4,Y1=3 → Y2 `""`.
  - `roundsToFourDecimals_trimsZeros` — X1=3,X2=1,Y1=1 → Y2 `"0.3333"`; X1=4,X2=2,Y1=1 → `"0.5"`.
  - `acceptsCommaAndDot_formatsWithSeparator` — X1=`"1,5"`,X2=`"3"`,Y1=`"2"`, sep `','` → Y2 `"4"`; X1=3,X2=1,Y1=1, sep `','` → `"0,3333"`.
- [ ] **Step 2: RED** — `./gradlew testDebugUnitTest` → compile FAIL (no `Proportion`); after an empty stub, assertion FAILs.
- [ ] **Step 3: Implement**; **Step 4: GREEN** — `./gradlew testDebugUnitTest` → all pass.

### Task 3: Proportion screen

**Files:** Create `ui/ProportionScreen.kt`; modify `ui/TokiToolsApp.kt` (tool body), strings; Test `ProportionScreenTest.kt` (rule `AppLanguageRule("hu")`)

**Interfaces:** `@Composable fun ProportionScreen(modifier: Modifier = Modifier)` holding `rememberSaveable(stateSaver = …)` state; fields tagged `"proportion-${field.name}"`; separator from `DecimalFormatSymbols.getInstance(LocalConfiguration.current.locales[0])`. `TokiToolsApp` body: `stateHolder.SaveableStateProvider(tool.name) { when (tool) { Proportion -> ProportionScreen(…); Percentage -> Box(…) } }`.

- [ ] **Step 1: Failing tests** — `fillingThree_showsFourth` (X1 `2`, X2 `4`, Y1 `3` → Y2 editable text `"6"`); `hungarianComma` (X1 `3`, X2 `1`, Y1 `1` → Y2 `"0,3333"`); `clear_emptiesAll`; `values_surviveToolSwitch` (fill, switch to Percentage and back → Y2 `"6"`).
- [ ] **Step 2: Implement** layout: two `Column`s of `OutlinedTextField` (decimal keyboard, centered text, placeholder = field name, `isError = isInvalid`) with a `HorizontalDivider` between, `"="` between columns; result field text bold primary; `OutlinedButton(clear)` below.
- [ ] **Step 3: Verify on Mac** — `assembleDebug assembleDebugAndroidTest lintDebug testDebugUnitTest` → pass, lint 0.

### Task 4: Phone session

- [ ] Snapshot taken after all tests, before Tasks 1–3 production code. Script: RED (snapshot: `localeConfig_matchesSelectableLanguages`, `ProportionScreenTest`) → GREEN full suite (expect 24) → `installDebug` → `pm list packages` → app locales unchanged.
