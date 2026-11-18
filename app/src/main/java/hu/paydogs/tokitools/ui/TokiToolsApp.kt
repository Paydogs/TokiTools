package hu.paydogs.tokitools.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import hu.paydogs.tokitools.AppLanguage
import hu.paydogs.tokitools.Language
import hu.paydogs.tokitools.R
import hu.paydogs.tokitools.ui.theme.TokiToolsTheme
import kotlinx.coroutines.launch

internal const val SCREEN_TITLE_TAG = "screenTitle"

// Top app bars expose the experimental TopAppBarScrollBehavior type in their signature.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokiToolsApp() {
    var tool by rememberSaveable { mutableStateOf(Tool.Proportion) }
    // Settings is a sub-page over the current tool, so leaving it lands back on that tool.
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // Not observable state: a language change recreates the activity, which recomposes anyway.
    val appLanguage = remember(context) { AppLanguage(context) }

    BackHandler(enabled = settingsOpen) { settingsOpen = false }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // No opening swipe on Settings, but a drawer restored open (rotation mid-close) must
        // still close by swipe or scrim tap.
        gesturesEnabled = !settingsOpen || drawerState.isOpen,
        drawerContent = {
            ToolsDrawerSheet(
                drawerState = drawerState,
                selected = tool,
                onToolClick = { clicked: Tool ->
                    tool = clicked
                    settingsOpen = false
                    scope.launch { drawerState.close() }
                },
                onSettingsClick = {
                    settingsOpen = true
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(if (settingsOpen) R.string.settings else tool.title),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.testTag(SCREEN_TITLE_TAG),
                        )
                    },
                    navigationIcon = {
                        if (settingsOpen) {
                            IconButton(onClick = { settingsOpen = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back),
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = stringResource(R.string.open_menu),
                                )
                            }
                        }
                    },
                )
            },
        ) { innerPadding: PaddingValues ->
            val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
            if (settingsOpen) {
                SettingsScreen(
                    selected = appLanguage.current,
                    onSelect = { language: Language -> appLanguage.set(language) },
                    modifier = contentModifier,
                )
            } else {
                // Tool content goes here once the tools are defined.
                Box(modifier = contentModifier)
            }
        }
    }
}

@Preview
@Composable
private fun TokiToolsAppPreview() {
    TokiToolsTheme {
        TokiToolsApp()
    }
}
