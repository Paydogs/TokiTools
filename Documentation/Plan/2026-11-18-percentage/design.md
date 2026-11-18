# Percentage calculator — design

## Intent
The Percentage tool solves X · Y% = Z (Y percent of X is Z): fill any two, the third is calculated.

## Decisions
- Same rules as Proportion: the two most recently typed valid fields are inputs; typing into the result makes the older input the result; clearing/partial input never leaves a stale result.
- Z = X·Y/100, X = Z·100/Y, Y = Z·100/X; no result when that divisor is 0.
- Shares the decimal limit, `,`/`.` input, typing limit, bold values, shrink-to-fit, hint/focus behaviour and state keeping with Proportion.
- Layout: one clay card row `[X] [Y] % (=) [Z]`, then a Törlés/Clear button. No Remember list.
- Shared code: `calc/DecimalText.kt` (parse/format/limit) and `ui/CalcComponents.kt` (value box, "=" badge, card, tap-to-clear-focus).
- Testing paused at the user's request: verified by build + lint only.
