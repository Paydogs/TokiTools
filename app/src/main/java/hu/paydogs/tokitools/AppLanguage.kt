package hu.paydogs.tokitools

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList

enum class Language(val tag: String, val nativeName: String) {
    Hungarian("hu", "Magyar"),
    English("en", "English"),
}

/** The app's own UI language, stored by Android's per-app language setting. */
class AppLanguage(context: Context) {
    private val localeManager: LocaleManager = context.getSystemService(LocaleManager::class.java)

    val current: Language
        get() {
            val locales = localeManager.applicationLocales
            if (locales.isEmpty) return Language.Hungarian
            val tag = locales[0].language
            return Language.entries.firstOrNull { language: Language -> language.tag == tag }
                ?: Language.Hungarian
        }

    fun set(language: Language) {
        localeManager.applicationLocales = LocaleList.forLanguageTags(language.tag)
    }

    // Hungarian even on English phones, unless a language was already chosen.
    fun applyDefault() {
        if (localeManager.applicationLocales.isEmpty) set(Language.Hungarian)
    }
}
