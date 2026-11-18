package hu.paydogs.tokitools

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.paydogs.tokitools.ui.SCREEN_TITLE_TAG
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultLanguageTest {
    @get:Rule(order = 0)
    val language = AppLanguageRule(null)

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun firstLaunch_withoutAppLanguage_showsHungarian() {
        // Applying the default recreates the activity asynchronously.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodes(hasTestTag(SCREEN_TITLE_TAG) and hasText("Arány"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        assertEquals("hu", language.current())
    }
}
