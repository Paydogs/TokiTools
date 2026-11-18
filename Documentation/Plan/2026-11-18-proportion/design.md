# Proportion calculator — design

## Intent
The Proportion tool solves X1/X2 = Y1/Y2: fill any three fields, the fourth is calculated.

## Goals / non-goals
- Goal: four decimal fields laid out as two fractions with "=" between; result highlighted.
- Goal: the three most recently edited valid fields are inputs; the fourth is the result (typing into the result makes the oldest input the result).
- Goal: values survive rotation and switching tools.
- Non-goal: history, units, more than one proportion.

## Approaches
- **Chosen — plain-Kotlin model + thin Compose screen:** logic unit-tested on the JVM (no phone); state in `rememberSaveable`.
- ViewModel: extra dependency, nothing gained for one local screen.

## Decisions
- Model: per-field text + typed order. After every edit: 3 most recent valid typed fields → compute the other, write it as its text, drop it from the typed order. Fewer than 3 → a previously computed field is emptied.
- Exact decimals (`BigDecimal`), rounded to the Settings decimal limit (default 4, half-up), trailing zeros dropped.
- Input accepts `,` and `.`; output uses the app language's separator.
- No result when a denominator (X2/Y2) is exactly 0 (a value only rounded to 0 is shown) or a division by 0 is needed; anything but a plain decimal (incl. exponent notation) gets an error outline. Clearing or starting to type into the result field keeps the typed text.
- "Törlés"/"Clear" button empties all fields.
- A field's hint is hidden while it is focused; tapping empty space or opening the side panel clears focus and hides the keyboard.
- Per-tool state via `rememberSaveableStateHolder`, keyed by tool.
- Look (user's "Proportion Pal" design, calculator part only): rounded clay card; each value in its own rounded clay box (default / typing pink / result mint / invalid red); yellow "=" badge; rounded fraction bars that turn pink/lavender when solved. No X₁ chips or ★. Dark palette from the user's "Midnight" design (`ui/ClayStyle.kt`). Buttons are clay buttons that sink when pressed: Clear soft blush, Remember blush, Clear list lavender. App-wide font: Comic Neue (SIL OFL stand-in for Comic Sans; license in `assets/licenses`), set in the theme typography.

## Left to the builder
Field width, spacing, fraction line drawing.
