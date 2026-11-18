package hu.paydogs.tokitools

import androidx.annotation.StringRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.paydogs.tokitools.ui.SCREEN_TITLE_TAG
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    // A set app language avoids the first-launch default recreating the activity mid-test.
    @get:Rule(order = 0)
    val language = AppLanguageRule("hu")

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    private fun str(@StringRes id: Int): String = rule.activity.getString(id)

    private fun title(): SemanticsNodeInteraction = rule.onNodeWithTag(SCREEN_TITLE_TAG)

    private fun item(@StringRes id: Int): SemanticsNodeInteraction =
        rule.onNode(hasText(str(id)) and hasClickAction())

    private fun upButton(): SemanticsNodeInteraction =
        rule.onNodeWithContentDescription(str(R.string.back))

    private fun openDrawer() {
        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()
    }

    private fun openSettings() {
        openDrawer()
        rule.onNodeWithContentDescription(str(R.string.settings)).performClick()
    }

    private fun switchToPercentage() {
        openDrawer()
        item(R.string.percentage).performClick()
    }

    @Test
    fun drawer_showsToolsHeadingAndVersion() {
        openDrawer()

        rule.onNodeWithText(str(R.string.tools)).assertIsDisplayed()
        rule.onNodeWithText("v" + BuildConfig.VERSION_NAME).assertIsDisplayed()
    }

    @Test
    fun settingsButton_closesDrawerAndShowsSettings() {
        openSettings()

        title().assertTextEquals(str(R.string.settings))
        item(R.string.proportion).assertIsNotDisplayed()
        upButton().assertIsDisplayed()
    }

    @Test
    fun back_onSettings_returnsToPreviousTool() {
        switchToPercentage()
        openSettings()

        Espresso.pressBack()

        title().assertTextEquals(str(R.string.percentage))
    }

    @Test
    fun upButton_onSettings_returnsToPreviousTool() {
        switchToPercentage()
        openSettings()

        upButton().performClick()

        title().assertTextEquals(str(R.string.percentage))
        rule.onNodeWithContentDescription(str(R.string.open_menu)).assertIsDisplayed()
    }

    @Test
    fun settings_survivesRecreation() {
        switchToPercentage()
        openSettings()
        // Let the drawer finish closing; recreating mid-animation restores it open.
        rule.waitForIdle()

        rule.activityRule.scenario.recreate()

        title().assertTextEquals(str(R.string.settings))
        upButton().performClick()
        title().assertTextEquals(str(R.string.percentage))
    }

    @Test
    fun swipe_onSettings_doesNotOpenDrawer() {
        openSettings()

        rule.onRoot().performTouchInput { swipeRight(startX = centerX / 2, endX = right) }

        item(R.string.proportion).assertIsNotDisplayed()
    }

    // Recreating while the drawer is still closing restores it open over Settings.
    private fun openSettingsAndRecreateMidAnimation() {
        openSettings()
        rule.activityRule.scenario.recreate()
        item(R.string.proportion).assertIsDisplayed()
    }

    @Test
    fun toolTap_whenDrawerRestoredOpenOnSettings_showsTool() {
        openSettingsAndRecreateMidAnimation()

        item(R.string.percentage).performClick()

        title().assertTextEquals(str(R.string.percentage))
    }

    @Test
    fun scrimTap_whenDrawerRestoredOpenOnSettings_closesDrawer() {
        openSettingsAndRecreateMidAnimation()

        rule.onRoot().performTouchInput { click(Offset(right - 5f, centerY)) }

        item(R.string.proportion).assertIsNotDisplayed()
    }
}
