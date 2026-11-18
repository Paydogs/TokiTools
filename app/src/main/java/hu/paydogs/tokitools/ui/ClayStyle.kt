package hu.paydogs.tokitools.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One raised "clay" surface: fill, outline, the darker lip below it, and the text on it. */
@Immutable
data class ClayColors(val fill: Color,
                      val border: Color,
                      val lip: Color,
                      val content: Color)

/** The calculator's pastel palette, from the user's light and dark ("Midnight") designs. */
@Immutable
data class CalculatorPalette(val card: ClayColors,
                             val box: ClayColors,
                             val boxActive: ClayColors,
                             val boxSolved: ClayColors,
                             val boxError: ClayColors,
                             val activeGlow: Color,
                             val equals: ClayColors,
                             val bar: Color,
                             val barSolvedLeft: Color,
                             val barSolvedRight: Color,
                             val placeholder: Color,
                             val caret: Color,
                             val clearButton: ClayColors,
                             val rememberButton: ClayColors,
                             val clearListButton: ClayColors) {
    companion object {
        private val Cocoa = Color(0xFF433633)
        private val Blush = Color(0xFFFF8FA3)

        val Light = CalculatorPalette(
            card = ClayColors(Color(0xFFFFFFFF), Color(0xFFF2E2DA), Color(0xFFEDE0D8), Cocoa),
            box = ClayColors(Color(0xFFFFFDFB), Color(0xFFEAD9D0), Color(0xFFE3CFC5), Cocoa),
            boxActive = ClayColors(Color(0xFFFFF4F6), Blush, Blush, Cocoa),
            boxSolved = ClayColors(Color(0xFFD8F3DC), Color(0xFF52B788), Color(0xFF74C69D), Color(0xFF1B4332)),
            boxError = ClayColors(Color(0xFFFFE5EC), Color(0xFFFF5C7A), Blush, Color(0xFFA4133C)),
            activeGlow = Color(0x8CFFCCD5),
            equals = ClayColors(Color(0xFFFFE599), Color(0xFFF2CE63), Color(0xFFE3BD4B), Cocoa),
            bar = Color(0xFFE6D5CD),
            barSolvedLeft = Color(0xFFFFCCD5),
            barSolvedRight = Color(0xFFC8B6FF),
            placeholder = Color(0xFFC7B3AD),
            caret = Blush,
            clearButton = ClayColors(Color(0xFFFFE5EA), Color(0xFFFFC2CE), Color(0xFFF5AEBC), Cocoa),
            rememberButton = ClayColors(Blush, Color(0xFFF4768D), Color(0xFFDF6279), Color.White),
            clearListButton = ClayColors(Color(0xFFECE4FF), Color(0xFFC8B6FF), Color(0xFFB39DF7), Cocoa),
        )

        // "Midnight" tokens from the user's dark design.
        val Dark = CalculatorPalette(
            card = ClayColors(Color(0xFF292140), Color(0xFF3B3059), Color(0xFF151024), Color(0xFFFBF7FF)),
            box = ClayColors(Color(0xFF241D38), Color(0xFF3A2F58), Color(0xFF151024), Color(0xFFFBF7FF)),
            boxActive = ClayColors(Color(0xFF35223D), Color(0xFFFF7EB6), Color(0xFFB84276), Color(0xFFFBF7FF)),
            boxSolved = ClayColors(Color(0xFF1B3834), Color(0xFF64F0C3), Color(0xFF269E78), Color(0xFF64F0C3)),
            boxError = ClayColors(Color(0xFF3D1F2D), Color(0xFFFF7EB6), Color(0xFF9E335A), Color(0xFFFFA6C9)),
            activeGlow = Color(0x38FF7EB6),
            equals = ClayColors(Color(0xFF3B3126), Color(0xFFFFD66B), Color(0xFF1C1610), Color(0xFFFFF0C2)),
            bar = Color(0xFF423663),
            barSolvedLeft = Color(0xFFFF7EB6),
            barSolvedRight = Color(0xFFB99AFF),
            placeholder = Color(0xFF7C709E),
            caret = Color(0xFFFF7EB6),
            clearButton = ClayColors(Color(0xFF3D243C), Color(0xFFFF7EB6), Color(0xFF1E1021), Color(0xFFFFD6E8)),
            rememberButton = ClayColors(Color(0xFFFF7EB6), Color(0xFFFFA6CE), Color(0xFFB84276), Color(0xFF1B101E)),
            clearListButton = ClayColors(Color(0xFF31264F), Color(0xFFB99AFF), Color(0xFF171129), Color(0xFFE9DFFF)),
        )
    }
}

/** Follows the app's resolved theme (it can differ from the phone's). */
@Composable
fun calculatorPalette(): CalculatorPalette =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) CalculatorPalette.Dark else CalculatorPalette.Light

/**
 * Raised "clay" look: [shape] filled and outlined, with a darker copy [lipHeight] below it
 * (space for the lip is reserved as bottom padding). [glow] draws a soft ring around it;
 * [sink] moves the surface down onto its lip without changing the layout (pressed buttons).
 */
fun Modifier.clay(colors: ClayColors,
                  shape: Shape,
                  borderWidth: Dp = 2.dp,
                  lipHeight: Dp = 4.dp,
                  glow: Color? = null,
                  sink: Dp = 0.dp): Modifier =
    this
        .padding(bottom = lipHeight)
        .drawBehind {
            val lip = lipHeight.toPx()
            if (glow != null) {
                val ring = GlowWidth.toPx()
                val glowSize = Size(size.width + 2 * ring, size.height + lip + 2 * ring)
                translate(left = -ring, top = -ring) {
                    drawOutline(shape.createOutline(glowSize, layoutDirection, this), glow)
                }
            }
            translate(top = lip) { drawOutline(shape.createOutline(size, layoutDirection, this), colors.lip) }
        }
        .graphicsLayer { translationY = sink.toPx() }
        .border(borderWidth, colors.border, shape)
        .background(colors.fill, shape)

private val GlowWidth = 4.dp

private val ButtonShape = RoundedCornerShape(16.dp)
private val ButtonLip = 4.dp

/** Tactile button from the design: sinks 3dp onto its lip while pressed; faded when disabled. */
@Composable
fun ClayButton(text: String,
               colors: ClayColors,
               onClick: () -> Unit,
               modifier: Modifier = Modifier,
               enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val sink by animateDpAsState(targetValue = if (pressed) 3.dp else 0.dp, label = "clayButtonSink")
    Box(contentAlignment = Alignment.Center,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clay(colors, ButtonShape, lipHeight = ButtonLip, sink = sink)
            .clickable(interactionSource = interactionSource,
                       indication = null,
                       enabled = enabled,
                       role = Role.Button,
                       onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = text,
             style = MaterialTheme.typography.labelLarge,
             fontSize = 16.sp,
             fontWeight = FontWeight.ExtraBold,
             color = colors.content)
    }
}
