package hu.paydogs.tokitools

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesTest {
    // Hungarian, so results use a decimal comma.
    @get:Rule(order = 0)
    val language = AppLanguageRule("hu")

    @get:Rule(order = 1)
    val settings = SettingsPrefsRule()

    @get:Rule(order = 2)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun str(@StringRes id: Int): String = rule.activity.getString(id)

    private fun openSettings() {
        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()
        rule.onNodeWithContentDescription(str(R.string.settings)).performClick()
    }

    private fun closeSettings() {
        rule.onNodeWithContentDescription(str(R.string.back)).performClick()
    }

    private fun fewer(): SemanticsNodeInteraction = rule.onNodeWithContentDescription(str(R.string.decimals_less))

    private fun more(): SemanticsNodeInteraction = rule.onNodeWithContentDescription(str(R.string.decimals_more))

    private fun decimalsValue(): SemanticsNodeInteraction = rule.onNodeWithTag("decimals-value")

    private fun field(name: String): SemanticsNodeInteraction = rule.onNodeWithTag("proportion-$name")

    // One character at a time, as a keyboard delivers it.
    private fun typeKeys(name: String, text: String) {
        text.forEach { character: Char -> field(name).performTextInput(character.toString()) }
    }

    private fun assertValue(name: String, expected: String) {
        field(name).assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(expected)))
    }

    private fun fontSize(name: String): TextUnit {
        val layouts = mutableListOf<TextLayoutResult>()
        field(name).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        return layouts.first().layoutInput.style.fontSize
    }

    @Test
    fun theme_choiceSurvivesRecreation() {
        openSettings()
        rule.onNodeWithText(str(R.string.theme_dark)).performClick()
        rule.onNodeWithText(str(R.string.theme_dark)).assertIsSelected()
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()

        rule.onNodeWithText(str(R.string.theme_dark)).assertIsSelected()
    }

    @Test
    fun decimals_stepperStopsAtBounds() {
        openSettings()
        decimalsValue().assertTextEquals("4")

        repeat(4) { fewer().performClick() }
        decimalsValue().assertTextEquals("0")
        fewer().assertIsNotEnabled()

        repeat(8) { more().performClick() }
        decimalsValue().assertTextEquals("8")
        more().assertIsNotEnabled()
    }

    @Test
    fun decimals_roundProportionResult() {
        openSettings()
        repeat(2) { fewer().performClick() }
        closeSettings()

        typeKeys("X1", "3")
        typeKeys("X2", "1")
        typeKeys("Y1", "1")

        assertValue("Y2", "0,33")
    }

    @Test
    fun decimals_limitTyping() {
        openSettings()
        repeat(2) { fewer().performClick() }
        closeSettings()

        typeKeys("X1", "1,234")

        assertValue("X1", "1,23")
    }

    @Test
    fun longNumber_shrinksFont() {
        field("X1").performTextInput("123456789012")

        val size = fontSize("X1")
        assertTrue("$size should be below 32.sp", size.value < 32f)
        assertTrue("$size should be at least 16.sp", size.value >= 16f)
    }

    @Test
    fun shortNumber_keepsFullSize() {
        field("X1").performTextInput("7")

        assertEquals(32.sp, fontSize("X1"))
    }

    private fun lightStatusBarIcons(): Boolean {
        var light = false
        rule.activityRule.scenario.onActivity { activity: MainActivity ->
            light = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                .isAppearanceLightStatusBars
        }
        return light
    }

    // Dark icons on a light theme and vice versa, whatever mode the phone itself is in.
    @Test
    fun theme_setsStatusBarIconContrast() {
        openSettings()

        rule.onNodeWithText(str(R.string.theme_light)).performClick()
        rule.waitForIdle()
        assertTrue(lightStatusBarIcons())

        rule.onNodeWithText(str(R.string.theme_dark)).performClick()
        rule.waitForIdle()
        assertFalse(lightStatusBarIcons())
    }

    @Test
    fun decimals_changeRecalculatesShownResult() {
        typeKeys("X1", "3")
        typeKeys("X2", "1")
        typeKeys("Y1", "1")
        assertValue("Y2", "0,3333")

        openSettings()
        repeat(2) { fewer().performClick() }
        closeSettings()

        assertValue("Y2", "0,33")
    }
}
