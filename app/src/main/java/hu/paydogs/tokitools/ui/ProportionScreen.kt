package hu.paydogs.tokitools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.paydogs.tokitools.R
import hu.paydogs.tokitools.proportion.Proportion
import hu.paydogs.tokitools.proportion.ProportionField
import hu.paydogs.tokitools.proportion.RememberedProportion

// Saved as 4 texts (field order), typed field names, result field name.
private val ProportionSaver = listSaver<Proportion, String>(
    save = { proportion: Proportion ->
        ProportionField.entries.map { field: ProportionField -> proportion.texts[field].orEmpty() } +
            proportion.typed.joinToString(",") { field: ProportionField -> field.name } +
            proportion.result?.name.orEmpty()
    },
    restore = { saved: List<String> ->
        Proportion(
            texts = ProportionField.entries.zip(saved.take(4)).toMap(),
            typed = saved[4].split(",").filter { name: String -> name.isNotEmpty() }.map(ProportionField::valueOf),
            result = saved[5].takeIf { name: String -> name.isNotEmpty() }?.let(ProportionField::valueOf),
        )
    },
)

@Composable
fun ProportionScreen(decimals: Int,
                     history: List<RememberedProportion>,
                     onRemember: (RememberedProportion) -> Unit,
                     onClearHistory: () -> Unit,
                     modifier: Modifier = Modifier) {
    var proportion by rememberSaveable(stateSaver = ProportionSaver) { mutableStateOf(Proportion.Empty) }
    val separator = appDecimalSeparator()
    val onEdit = { field: ProportionField, text: String ->
        proportion = proportion.edit(field, text, separator, decimals)
    }
    // A restored result may have been computed with another limit or language.
    LaunchedEffect(separator, decimals) { proportion = proportion.recalculated(separator, decimals) }
    val focusManager = LocalFocusManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally,
           modifier = modifier
               .clearFocusOnTap(focusManager)
               .padding(top = 32.dp)
               .padding(24.dp)) {
        val palette = calculatorPalette()
        val solved = proportion.remembered() != null
        CalculatorCard(palette = palette) {
            Fraction(top = ProportionField.X1,
                     bottom = ProportionField.X2,
                     barColor = if (solved) palette.barSolvedLeft else palette.bar,
                     proportion = proportion,
                     palette = palette,
                     onEdit = onEdit,
                     modifier = Modifier.weight(1f))
            EqualsBadge(palette = palette)
            Fraction(top = ProportionField.Y1,
                     bottom = ProportionField.Y2,
                     barColor = if (solved) palette.barSolvedRight else palette.bar,
                     proportion = proportion,
                     palette = palette,
                     onEdit = onEdit,
                     modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        ClayButton(text = stringResource(R.string.clear),
                   colors = palette.clearButton,
                   onClick = { proportion = Proportion.Empty },
                   modifier = Modifier.widthIn(min = CalcButtonMinWidth))
        Spacer(modifier = Modifier.height(24.dp))
        val complete = proportion.remembered()
        ClayButton(text = stringResource(R.string.remember),
                   colors = palette.rememberButton,
                   onClick = { complete?.let(onRemember) },
                   enabled = complete != null,
                   modifier = Modifier.widthIn(min = CalcButtonMinWidth))
        Spacer(modifier = Modifier.height(16.dp))
        // Takes the free space so "Clear list" stays at the bottom.
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(items = history, key = { entry: RememberedProportion -> entry.label }) { entry: RememberedProportion ->
                Text(text = entry.label,
                     style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
                     modifier = Modifier
                         .fillMaxWidth()
                         .clickable {
                             focusManager.clearFocus()
                             proportion = Proportion.restored(entry)
                         }
                         .padding(vertical = 12.dp, horizontal = 8.dp))
                HorizontalDivider()
            }
        }
        ClayButton(text = stringResource(R.string.clear_list),
                   colors = palette.clearListButton,
                   onClick = onClearHistory,
                   enabled = history.isNotEmpty(),
                   modifier = Modifier.padding(top = 8.dp).widthIn(min = CalcButtonMinWidth))
    }
}

@Composable
private fun Fraction(top: ProportionField,
                     bottom: ProportionField,
                     barColor: Color,
                     proportion: Proportion,
                     palette: CalculatorPalette,
                     onEdit: (ProportionField, String) -> Unit,
                     modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp),
           modifier = modifier) {
        ProportionValue(field = top, proportion = proportion, palette = palette, onEdit = onEdit)
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .background(barColor, CircleShape))
        ProportionValue(field = bottom, proportion = proportion, palette = palette, onEdit = onEdit)
    }
}

@Composable
private fun ProportionValue(field: ProportionField,
                            proportion: Proportion,
                            palette: CalculatorPalette,
                            onEdit: (ProportionField, String) -> Unit) {
    val text = proportion.texts[field].orEmpty()
    ValueBox(label = field.name,
             text = text,
             isResult = proportion.result == field && text.isNotBlank(),
             isError = proportion.isInvalid(field),
             palette = palette,
             onValueChange = { changed: String -> onEdit(field, changed) },
             testTag = "proportion-${field.name}")
}
