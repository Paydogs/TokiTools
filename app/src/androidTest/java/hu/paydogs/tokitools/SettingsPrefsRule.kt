package hu.paydogs.tokitools

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.test.platform.app.InstrumentationRegistry
import hu.paydogs.tokitools.settings.SettingsStore
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Starts each test with default settings and restores the user's saved settings afterwards. */
class SettingsPrefsRule : TestWatcher() {
    private val prefs: SharedPreferences = InstrumentationRegistry.getInstrumentation()
        .targetContext
        .getSharedPreferences(SettingsStore.PREFS_NAME, Context.MODE_PRIVATE)
    private var saved: Map<String, Any?> = emptyMap()

    override fun starting(description: Description) {
        saved = prefs.all.toMap()
        prefs.edit(commit = true) { clear() }
    }

    override fun finished(description: Description) {
        prefs.edit(commit = true) {
            clear()
            saved.forEach { (key: String, value: Any?) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
    }
}
