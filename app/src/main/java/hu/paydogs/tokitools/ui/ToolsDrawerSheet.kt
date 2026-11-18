package hu.paydogs.tokitools.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hu.paydogs.tokitools.BuildConfig
import hu.paydogs.tokitools.R

// Lines text up with the item labels: 12dp item padding + 16dp label inset.
private val LabelInset = 28.dp

@Composable
fun ToolsDrawerSheet(drawerState: DrawerState,
                     selected: Tool,
                     onToolClick: (Tool) -> Unit,
                     onSettingsClick: () -> Unit) {
    // Passing drawerState makes the sheet close itself on Back.
    ModalDrawerSheet(drawerState = drawerState) {
        Text(
            text = stringResource(R.string.tools),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = LabelInset, vertical = 16.dp),
        )
        Tool.entries.forEach { tool: Tool ->
            NavigationDrawerItem(
                label = { Text(stringResource(tool.title)) },
                selected = tool == selected,
                onClick = { onToolClick(tool) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = LabelInset, end = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
        }
    }
}
