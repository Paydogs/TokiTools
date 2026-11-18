package hu.paydogs.tokitools

import android.app.LocaleManager
import android.os.LocaleList
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the app language before the activity launches ([tag] null = none chosen) and restores
 * the previous choice afterwards, so test runs don't overwrite the user's language on the phone.
 */
class AppLanguageRule(private val tag: String?) : TestWatcher() {
    private val localeManager: LocaleManager = InstrumentationRegistry.getInstrumentation()
        .targetContext
        .getSystemService(LocaleManager::class.java)
    private var saved: LocaleList = LocaleList.getEmptyLocaleList()

    override fun starting(description: Description) {
        saved = localeManager.applicationLocales
        localeManager.applicationLocales = tag?.let { value: String -> LocaleList.forLanguageTags(value) }
            ?: LocaleList.getEmptyLocaleList()
    }

    override fun finished(description: Description) {
        localeManager.applicationLocales = saved
    }

    fun current(): String = localeManager.applicationLocales.toLanguageTags()
}
