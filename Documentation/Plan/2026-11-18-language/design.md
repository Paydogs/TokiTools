# In-app language selector — design

## Intent
Let the user switch TökiTools between Hungarian and English inside the app; Hungarian by default, even on an English phone.

## Goals / non-goals
- Goal: Settings shows "Nyelv"/"Language" with Magyar / English (native names); choosing applies immediately and stays on Settings.
- Goal: first start with no app language set → Hungarian.
- Goal: phone Settings → Apps → TökiTools → Language lists exactly Hungarian and English.
- Non-goal: Android 12 support; a "follow system" option.

## Approaches
- **Chosen — platform per-app language (`LocaleManager`, Android 13+):** Android persists the choice and shows it in system settings; least code. minSdk 31 → 33.
- TimeUntil's `LocaleStore` (prefs + context wrapping): works on Android 12, more code, invisible to system settings.
- AppCompat backport: needs AppCompatActivity and an AppCompat theme.

## Decisions
- `AppLanguage` wraps `LocaleManager`; UI never calls the system service directly.
- Default applied in `MainActivity.onCreate` when `applicationLocales` is empty (one-time recreation). Picking "System default" in phone settings is overridden back to Hungarian on next launch.
- `generateLocaleConfig = true` with `unqualifiedResLocale=en-US`.
- Settings content in its own composable.
- Tests set the app language before launch and restore the user's choice afterwards.

## Left to the builder
Radio row layout and spacing.
