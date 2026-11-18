# App icon — notes

- Source: the user's 2000×2000 frameless clay "X₁/X₂ = Y₁/Y₂" art on a full-bleed night sky (not stored in the repo). Replaces an earlier framed version.
- Foreground (`mipmap-*/ic_launcher_foreground.png`): the whole art scaled to 80% of the 108dp adaptive layer, padded with `#1E193D`, so the characters keep a margin inside the 72dp visible area.
- Background: `@color/ic_launcher_background` `#1E193D` (art edge colour).
- Monochrome (themed icons): `drawable/ic_launcher_monochrome.xml`, a simple "•/• = •/•" silhouette — our own, not from the artwork.
- Circle-mask launchers may clip the outer tips of X₁/Y₁ slightly; Samsung's squircle shows them.
