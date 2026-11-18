package hu.paydogs.tokitools.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import hu.paydogs.tokitools.R

// Comic Neue: an open-source (SIL OFL, license in assets/licenses) stand-in for Comic Sans,
// which can't be bundled. Weights between the two files resolve to the nearest one.
private val ComicNeue = FontFamily(
    Font(R.font.comic_neue_regular, FontWeight.Normal),
    Font(R.font.comic_neue_bold, FontWeight.Bold),
)

// Wider than Material's defaults (≈0.03em) on purpose; em keeps it proportional at every size.
private val LetterSpacing = 0.06.em

private fun TextStyle.comic(): TextStyle = copy(fontFamily = ComicNeue, letterSpacing = LetterSpacing)

// Material's default sizes and weights, all in Comic Neue with wider letter spacing.
val Typography = Typography().let { base: Typography ->
    base.copy(
        displayLarge = base.displayLarge.comic(),
        displayMedium = base.displayMedium.comic(),
        displaySmall = base.displaySmall.comic(),
        headlineLarge = base.headlineLarge.comic(),
        headlineMedium = base.headlineMedium.comic(),
        headlineSmall = base.headlineSmall.comic(),
        titleLarge = base.titleLarge.comic(),
        titleMedium = base.titleMedium.comic(),
        titleSmall = base.titleSmall.comic(),
        bodyLarge = base.bodyLarge.comic(),
        bodyMedium = base.bodyMedium.comic(),
        bodySmall = base.bodySmall.comic(),
        labelLarge = base.labelLarge.comic(),
        labelMedium = base.labelMedium.comic(),
        labelSmall = base.labelSmall.comic(),
    )
}
