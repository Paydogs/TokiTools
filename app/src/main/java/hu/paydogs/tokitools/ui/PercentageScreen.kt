package hu.paydogs.tokitools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.paydogs.tokitools.R
import hu.paydogs.tokitools.percentage.Percentage
import hu.paydogs.tokitools.percentage.PercentageField

// Saved as 3 texts (field order), typed field names, result field name.
private val PercentageSaver = listSaver<Percentage, String>(
    save = { percentage: Percentage ->
        PercentageField.entries.map { field: PercentageField -> percentage.texts[field].orEmpty() } +
            percentage.typed.joinToString(",") { field: PercentageField -> field.name } +
            percentage.result?.name.orEmpty()
    },
    restore = { saved: List<String> ->
        Percentage(
            texts = PercentageField.entries.zip(saved.take(3)).toMap(),
            typed = saved[3].split(",").filter { name: String -> name.isNotEmpty() }.map(PercentageField::valueOf),
            result = saved[4].takeIf { name: String -> name.isNotEmpty() }?.let(PercentageField::valueOf),
        )
    },
)

/** X · Y% = Z: any two values give the third. */
@Composable
fun PercentageScreen(decimals: Int,
                     modifier: Modifier = Modifier) {
    var percentage by rememberSaveable(stateSaver = PercentageSaver) { mutableStateOf(Percentage.Empty) }
    val separator = appDecimalSeparator()
    val onEdit = { field: PercentageField, text: String ->
        percentage = percentage.edit(field, text, separator, decimals)
    }
    // A restored result may have been computed with another limit or language.
    LaunchedEffect(separator, decimals) { percentage = percentage.recalculated(separator, decimals) }
    val focusManager = LocalFocusManager.current
    val palette = calculatorPalette()

    Column(horizontalAlignment = Alignment.CenterHorizontally,
           modifier = modifier
               .clearFocusOnTap(focusManager)
               .padding(top = 32.dp)
               .padding(24.dp)) {
        CalculatorCard(palette = palette) {
            PercentageValue(field = PercentageField.X,
                            percentage = percentage,
                            palette = palette,
                            onEdit = onEdit,
                            modifier = Modifier.weight(1f))
            // Extra gap so X and Y read as two separate values.
            Spacer(modifier = Modifier.width(10.dp))
            PercentageValue(field = PercentageField.Y,
                            percentage = percentage,
                            palette = palette,
                            onEdit = onEdit,
                            modifier = Modifier.weight(1f))
            Text(text = "%",
                 fontSize = 28.sp,
                 fontWeight = FontWeight.Bold,
                 color = palette.card.content)
            EqualsBadge(palette = palette)
            PercentageValue(field = PercentageField.Z,
                            percentage = percentage,
                            palette = palette,
                            onEdit = onEdit,
                            modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        ClayButton(text = stringResource(R.string.clear),
                   colors = palette.clearButton,
                   onClick = { percentage = Percentage.Empty },
                   modifier = Modifier.widthIn(min = CalcButtonMinWidth))
    }
}

@Composable
private fun PercentageValue(field: PercentageField,
                            percentage: Percentage,
                            palette: CalculatorPalette,
                            onEdit: (PercentageField, String) -> Unit,
                            modifier: Modifier = Modifier) {
    val text = percentage.texts[field].orEmpty()
    ValueBox(label = field.name,
             text = text,
             isResult = percentage.result == field && text.isNotBlank(),
             isError = percentage.isInvalid(field),
             palette = palette,
             onValueChange = { changed: String -> onEdit(field, changed) },
             testTag = "percentage-${field.name}",
             modifier = modifier)
}
