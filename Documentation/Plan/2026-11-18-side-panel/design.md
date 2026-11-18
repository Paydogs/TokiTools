# Side panel navigation — design

## Intent
TokiTools will hold several small tools. A left side panel switches between them.
First two: Proportion and Percentage, placeholders until their content is defined.

## Goals / non-goals
- Goal: drawer with Proportion and Percentage; tapping one closes the drawer and shows that screen.
- Goal: each screen shows its name top-center in a large font.
- Non-goal: any calculator content; navigation library; back stack between tools.

## Approaches
- **Chosen — `enum` + `when`:** same as MagniTools, no dependency; fits two equal top-level screens.
- Navigation 3 / Navigation Compose: worth it with stacked screens or deep links; neither exists. Easy to adopt later.

## Decisions
- Material 3 `ModalNavigationDrawer`; opened by ☰ in a `CenterAlignedTopAppBar` or a right swipe.
  With gesture navigation the left-edge strip is system Back; a swipe starting further in opens the drawer.
- Top bar title = screen name, large font; body empty.
- Current item highlighted. Launch on Proportion; selection survives rotation.
- Back: closes drawer if open, otherwise exits.
- Names in string resources with Hungarian: Arány, Százalék (lint `MissingTranslation`).
- Remove `Greeting` / `GreetingPreview`.
- Verified by an instrumented Compose UI test on the phone.

## Left to the builder
File layout, title text style, drawer width/padding.
