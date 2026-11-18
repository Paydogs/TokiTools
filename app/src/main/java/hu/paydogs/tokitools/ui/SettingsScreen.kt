package hu.paydogs.tokitools.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import hu.paydogs.tokitools.Language
import hu.paydogs.tokitools.R

@Composable
fun SettingsScreen(selected: Language,
                   onSelect: (Language) -> Unit,
                   modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Column(modifier = Modifier.selectableGroup()) {
            Language.entries.forEach { language: Language ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = language == selected,
                            onClick = { onSelect(language) },
                            role = Role.RadioButton,
                        ),
                ) {
                    // The whole row handles the click; the button only shows the state.
                    RadioButton(selected = language == selected, onClick = null)
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}
