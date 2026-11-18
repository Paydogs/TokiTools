package hu.paydogs.tokitools

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.paydogs.tokitools.ui.SCREEN_TITLE_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Literal texts on purpose: which language is shown is the behaviour under test.
@RunWith(AndroidJUnit4::class)
class LanguageTest {
    @get:Rule(order = 0)
    val language = AppLanguageRule("hu")

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun str(@StringRes id: Int): String = rule.activity.getString(id)

    private fun openSettings() {
        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()
        rule.onNodeWithContentDescription(str(R.string.settings)).performClick()
    }

    // A language change recreates the activity asynchronously, outside Compose's idle tracking.
    private fun awaitTitle(text: String) {
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodes(hasTestTag(SCREEN_TITLE_TAG) and hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun settings_marksCurrentLanguage() {
        openSettings()

        rule.onNodeWithText("Magyar").assertIsSelected()
        rule.onNodeWithText("English").assertIsNotSelected()
    }

    @Test
    fun choosingEnglish_switchesAndStaysOnSettings() {
        openSettings()

        rule.onNodeWithText("English").performClick()

        awaitTitle("Settings")
        rule.onNodeWithText("English").assertIsSelected()
        assertEquals("en", language.current())
    }

    @Test
    fun choosingMagyar_afterEnglish_switchesBack() {
        openSettings()
        rule.onNodeWithText("English").performClick()
        awaitTitle("Settings")

        rule.onNodeWithText("Magyar").performClick()

        awaitTitle("Beállítások")
        assertEquals("hu", language.current())
    }
}
