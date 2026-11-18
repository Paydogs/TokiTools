package hu.paydogs.tokitools.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormatSymbols

// Pieces shared by the calculator screens (Proportion, Percentage).

private val MaxFontSize = 32.sp
private val MinFontSize = 16.sp
private val CardShape = RoundedCornerShape(26.dp)
private val BoxShape = RoundedCornerShape(22.dp)
private val BadgeShape = RoundedCornerShape(16.dp)

// Equal widths keep stacked buttons tidy.
internal val CalcButtonMinWidth = 180.dp

/** Results use the app language's decimal separator (comma in Hungarian). */
@Composable
internal fun appDecimalSeparator(): Char =
    DecimalFormatSymbols.getInstance(LocalConfiguration.current.locales[0]).decimalSeparator

/** Tapping empty space leaves the field and hides the keyboard; fields consume their own taps. */
internal fun Modifier.clearFocusOnTap(focusManager: FocusManager): Modifier =
    pointerInput(Unit) { detectTapGestures(onTap = { _: Offset -> focusManager.clearFocus() }) }

/** The rounded clay card that holds a calculator's boxes. */
@Composable
internal fun CalculatorCard(palette: CalculatorPalette,
                            content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clay(palette.card, CardShape, lipHeight = 5.dp)
            .padding(12.dp),
        content = content)
}

@Composable
internal fun EqualsBadge(palette: CalculatorPalette) {
    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .clay(palette.equals, BadgeShape)
            .size(44.dp)) {
        Text(text = "=",
             fontSize = 24.sp,
             fontWeight = FontWeight.Bold,
             color = palette.equals.content)
    }
}

/**
 * One editable value in its own clay box: pink while typing, mint when it is the calculated
 * result, red when invalid. [label] is the hint shown while empty and unfocused.
 */
@Composable
internal fun ValueBox(label: String,
                      text: String,
                      isResult: Boolean,
                      isError: Boolean,
                      palette: CalculatorPalette,
                      onValueChange: (String) -> Unit,
                      testTag: String,
                      modifier: Modifier = Modifier) {
    // Twice the default field text size; the hint shares it so both line up.
    val fieldTextStyle = LocalTextStyle.current.copy(fontSize = MaxFontSize, lineHeight = 40.sp)
    // All values bold, as in the design; the result stands out by its box colour instead.
    val valueStyle = fieldTextStyle.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val measurer = rememberTextMeasurer()
        // The text area is the box minus its side padding and borders.
        val availableWidth = constraints.maxWidth - with(LocalDensity.current) { 26.dp.roundToPx() }
        val fontSize = remember(text, valueStyle, availableWidth) {
            fittingFontSize(text = text, style = valueStyle, maxWidth = availableWidth, measurer = measurer)
        }
        ValueTextField(label = label,
                       text = text,
                       hintStyle = fieldTextStyle,
                       valueStyle = valueStyle.copy(fontSize = fontSize),
                       isResult = isResult,
                       isError = isError,
                       palette = palette,
                       onValueChange = onValueChange,
                       testTag = testTag)
    }
}

@Composable
private fun ValueTextField(label: String,
                           text: String,
                           hintStyle: TextStyle,
                           valueStyle: TextStyle,
                           isResult: Boolean,
                           isError: Boolean,
                           palette: CalculatorPalette,
                           onValueChange: (String) -> Unit,
                           testTag: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = when {
        isError -> palette.boxError
        focused -> palette.boxActive
        isResult -> palette.boxSolved
        else -> palette.box
    }
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        textStyle = valueStyle.copy(color = colors.content),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(palette.caret),
        modifier = Modifier.fillMaxWidth().testTag(testTag),
        decorationBox = { innerTextField: @Composable () -> Unit ->
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clay(colors, BoxShape, borderWidth = 2.5.dp, glow = if (focused) palette.activeGlow else null)
                    .heightIn(min = 72.dp)
                    .padding(horizontal = 10.dp, vertical = 8.dp)) {
                // The hint names the field only until the user starts entering it.
                if (text.isEmpty() && !focused) {
                    Text(text = label,
                         style = hintStyle,
                         color = palette.placeholder,
                         textAlign = TextAlign.Center,
                         modifier = Modifier.fillMaxWidth())
                }
                innerTextField()
            }
        },
    )
}

// Largest size, stepping down 2sp at a time, at which the text fits; never below MinFontSize.
private fun fittingFontSize(text: String,
                            style: TextStyle,
                            maxWidth: Int,
                            measurer: TextMeasurer): TextUnit {
    var size = MaxFontSize.value
    while (size > MinFontSize.value) {
        val width = measurer.measure(text = text, style = style.copy(fontSize = size.sp), maxLines = 1).size.width
        if (width <= maxWidth) break
        size -= 2f
    }
    return size.sp
}
