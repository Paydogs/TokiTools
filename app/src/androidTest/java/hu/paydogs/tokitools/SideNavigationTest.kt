package hu.paydogs.tokitools

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import hu.paydogs.tokitools.ui.SCREEN_TITLE_TAG
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SideNavigationTest {
    // A set app language avoids the first-launch default recreating the activity mid-test.
    @get:Rule(order = 0)
    val language = AppLanguageRule("hu")

    @get:Rule(order = 1)
    val rule = createAndroidComposeRule<MainActivity>()

    // Expected text comes from resources so the tests pass in any device language.
    private fun str(@StringRes id: Int): String = rule.activity.getString(id)

    private fun title(): SemanticsNodeInteraction = rule.onNodeWithTag(SCREEN_TITLE_TAG)

    private fun item(@StringRes id: Int): SemanticsNodeInteraction =
        rule.onNode(hasText(str(id)) and hasClickAction())

    private fun openDrawer() {
        rule.onNodeWithContentDescription(str(R.string.open_menu)).performClick()
    }

    @Test
    fun startsOnProportion() {
        title().assertTextEquals(str(R.string.proportion))
    }

    @Test
    fun tappingPercentage_closesDrawerAndShowsPercentage() {
        openDrawer()
        item(R.string.percentage).performClick()

        title().assertTextEquals(str(R.string.percentage))
        item(R.string.percentage).assertIsNotDisplayed()
    }

    @Test
    fun tappingCurrentItem_closesDrawerAndKeepsScreen() {
        openDrawer()
        item(R.string.proportion).performClick()

        title().assertTextEquals(str(R.string.proportion))
        item(R.string.proportion).assertIsNotDisplayed()
    }

    @Test
    fun drawer_highlightsCurrentScreen() {
        openDrawer()
        item(R.string.proportion).assertIsSelected()
        item(R.string.percentage).assertIsNotSelected()

        item(R.string.percentage).performClick()
        openDrawer()

        item(R.string.percentage).assertIsSelected()
        item(R.string.proportion).assertIsNotSelected()
    }

    @Test
    fun back_closesOpenDrawer() {
        openDrawer()
        item(R.string.percentage).assertIsDisplayed()

        Espresso.pressBack()

        item(R.string.percentage).assertIsNotDisplayed()
        assertFalse(rule.activity.isFinishing)
    }

    @Test
    fun selection_survivesRecreation() {
        openDrawer()
        item(R.string.percentage).performClick()

        rule.activityRule.scenario.recreate()

        title().assertTextEquals(str(R.string.percentage))
    }
}
