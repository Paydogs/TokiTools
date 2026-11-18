package hu.paydogs.tokitools

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProportionScreenTest {
    // Hungarian, so the decimal separator in results is a comma.
    @get:Rule(order = 0)
    val language = AppLanguageRule("hu")

    // Default decimals (4) regardless of what the user saved on the phone.
    @get:Rule(order = 1)
    val settings = SettingsPrefsRule()

    @get:Rule(order = 2)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun str(@StringRes id: Int): String = rule.activity.getString(id)

    private fun field(name: String): SemanticsNodeInteraction = rule.onNodeWithTag("proportion-$name")

    private fun type(name: String, text: String) {
        field(name).performTextInput(text)
    }

    private fun assertValue(name: String, expected: String) {
        field(name).assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(expected)))
    }

    private fun switchTool(@StringRes id: Int) {
        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()
        rule.onNode(hasText(str(id)) and hasClickAction()).performClick()
    }

    @Test
    fun fillingThree_showsFourth() {
        type("X1", "2")
        type("X2", "4")
        type("Y1", "3")

        assertValue("Y2", "6")
    }

    @Test
    fun hungarianResult_usesDecimalComma() {
        type("X1", "3")
        type("X2", "1")
        type("Y1", "1")

        assertValue("Y2", "0,3333")
    }

    @Test
    fun clear_emptiesAllFields() {
        type("X1", "2")
        type("X2", "4")
        type("Y1", "3")

        rule.onNodeWithText(str(R.string.clear)).performClick()

        listOf("X1", "X2", "Y1", "Y2").forEach { name: String -> assertValue(name, "") }
    }

    @Test
    fun values_surviveToolSwitch() {
        type("X1", "2")
        type("X2", "4")
        type("Y1", "3")

        switchTool(R.string.percentage)
        switchTool(R.string.proportion)

        assertValue("Y2", "6")
    }

    @Test
    fun focusedField_hidesPlaceholder() {
        field("X1").assert(hasText("X1"))

        field("X1").performClick()

        field("X1").assert(hasText("X1").not())
        field("X2").assert(hasText("X2"))
    }

    @Test
    fun backgroundTap_clearsFocus() {
        field("X1").performClick()
        field("X1").assertIsFocused()

        // The screen's centre is empty space below the Clear button.
        rule.onRoot().performTouchInput { click(center) }

        field("X1").assertIsNotFocused()
    }

    @Test
    fun openingMenu_clearsFocus() {
        field("X1").performClick()
        field("X1").assertIsFocused()

        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()

        field("X1").assertIsNotFocused()
    }
}
