package hu.paydogs.tokitools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import hu.paydogs.tokitools.Language
import hu.paydogs.tokitools.R
import hu.paydogs.tokitools.settings.ThemeMode
import hu.paydogs.tokitools.settings.UserSettings

@Composable
fun SettingsScreen(language: Language,
                   onLanguageSelect: (Language) -> Unit,
                   settings: UserSettings,
                   onSettingsChange: (UserSettings) -> Unit,
                   modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SectionHeading(stringResource(R.string.language))
        LanguageDropdown(selected = language, onSelect = onLanguageSelect)

        SectionHeading(stringResource(R.string.theme))
        RadioGroup(options = ThemeMode.entries,
                   selected = settings.theme,
                   label = { option: ThemeMode -> stringResource(option.label) },
                   onSelect = { theme: ThemeMode -> onSettingsChange(settings.copy(theme = theme)) })

        DecimalsStepper(decimals = settings.decimals,
                        onChange = { decimals: Int -> onSettingsChange(settings.copy(decimals = decimals)) })
    }
}

private val ThemeMode.label: Int
    get() = when (this) {
        ThemeMode.System -> R.string.theme_system
        ThemeMode.Light -> R.string.theme_light
        ThemeMode.Dark -> R.string.theme_dark
    }

// ExposedDropdownMenuBox is still experimental in material3 1.4.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(selected: Language,
                             onSelect: (Language) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded,
                           onExpandedChange = { open: Boolean -> expanded = open }) {
        OutlinedTextField(
            value = selected.nativeName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded,
                            onDismissRequest = { expanded = false }) {
            Language.entries.forEach { language: Language ->
                DropdownMenuItem(text = { Text(language.nativeName) },
                                 onClick = {
                                     expanded = false
                                     onSelect(language)
                                 })
            }
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(text = text,
         style = MaterialTheme.typography.titleMedium,
         modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
private fun <T> RadioGroup(options: List<T>,
                           selected: T,
                           label: @Composable (T) -> String,
                           onSelect: (T) -> Unit) {
    Column(modifier = Modifier.selectableGroup()) {
        options.forEach { option: T ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        role = Role.RadioButton,
                    ),
            ) {
                // The whole row handles the click; the button only shows the state.
                RadioButton(selected = option == selected, onClick = null)
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun DecimalsStepper(decimals: Int,
                            onChange: (Int) -> Unit) {
    val fewer = stringResource(R.string.decimals_less)
    val more = stringResource(R.string.decimals_more)
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(text = stringResource(R.string.decimals), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        OutlinedIconButton(onClick = { onChange(decimals - 1) },
                           enabled = decimals > UserSettings.MIN_DECIMALS,
                           modifier = Modifier.semantics { contentDescription = fewer }) {
            // The button's description is read instead of "minus".
            Text(text = "−", modifier = Modifier.clearAndSetSemantics {})
        }
        Text(text = decimals.toString(),
             style = MaterialTheme.typography.titleLarge,
             modifier = Modifier.padding(horizontal = 16.dp).testTag("decimals-value"))
        OutlinedIconButton(onClick = { onChange(decimals + 1) },
                           enabled = decimals < UserSettings.MAX_DECIMALS,
                           modifier = Modifier.semantics { contentDescription = more }) {
            Text(text = "+", modifier = Modifier.clearAndSetSemantics {})
        }
    }
}
