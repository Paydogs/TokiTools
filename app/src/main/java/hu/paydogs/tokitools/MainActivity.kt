package hu.paydogs.tokitools

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import hu.paydogs.tokitools.proportion.ProportionHistoryStore
import hu.paydogs.tokitools.proportion.RememberedProportion
import hu.paydogs.tokitools.proportion.withRemembered
import hu.paydogs.tokitools.settings.SettingsStore
import hu.paydogs.tokitools.settings.ThemeMode
import hu.paydogs.tokitools.settings.UserSettings
import hu.paydogs.tokitools.ui.TokiToolsApp
import hu.paydogs.tokitools.ui.theme.TokiToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appLanguage = AppLanguage(this)
        appLanguage.applyDefault()
        // Read once: a language change recreates the activity.
        val language = appLanguage.current
        val settingsStore = SettingsStore(this)
        val historyStore = ProportionHistoryStore(this)
        setContent {
            var settings by remember { mutableStateOf(settingsStore.load()) }
            var proportionHistory by remember { mutableStateOf(historyStore.load()) }
            val darkTheme = when (settings.theme) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            // System-bar icons must follow the app's theme, not only the phone's.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose {}
            }
            TokiToolsTheme(darkTheme = darkTheme) {
                TokiToolsApp(language = language,
                             onLanguageSelect = { selected: Language -> appLanguage.set(selected) },
                             settings = settings,
                             onSettingsChange = { changed: UserSettings ->
                                 settings = changed
                                 settingsStore.save(changed)
                             },
                             proportionHistory = proportionHistory,
                             onRememberProportion = { entry: RememberedProportion ->
                                 proportionHistory = proportionHistory.withRemembered(entry)
                                 historyStore.save(proportionHistory)
                             },
                             onClearProportionHistory = {
                                 proportionHistory = emptyList()
                                 historyStore.save(proportionHistory)
                             })
            }
        }
    }

    private companion object {
        // enableEdgeToEdge's default navigation-bar scrims.
        val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
