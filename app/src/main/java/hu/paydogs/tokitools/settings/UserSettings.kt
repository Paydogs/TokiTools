package hu.paydogs.tokitools.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

enum class ThemeMode { System, Light, Dark }

data class UserSettings(val theme: ThemeMode = ThemeMode.System,
                        val decimals: Int = DEFAULT_DECIMALS) {
    companion object {
        const val MIN_DECIMALS = 0
        const val MAX_DECIMALS = 8
        const val DEFAULT_DECIMALS = 4
    }
}

/** Synchronous so the first frame already uses the saved theme. */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): UserSettings {
        val theme = prefs.getString(KEY_THEME, null)
            ?.let { name: String -> ThemeMode.entries.firstOrNull { mode: ThemeMode -> mode.name == name } }
            ?: ThemeMode.System
        val decimals = prefs.getInt(KEY_DECIMALS, UserSettings.DEFAULT_DECIMALS)
            .coerceIn(UserSettings.MIN_DECIMALS, UserSettings.MAX_DECIMALS)
        return UserSettings(theme = theme, decimals = decimals)
    }

    fun save(settings: UserSettings) {
        prefs.edit {
            putString(KEY_THEME, settings.theme.name)
            putInt(KEY_DECIMALS, settings.decimals)
        }
    }

    companion object {
        const val PREFS_NAME = "settings"
        private const val KEY_THEME = "theme"
        private const val KEY_DECIMALS = "decimals"
    }
}
