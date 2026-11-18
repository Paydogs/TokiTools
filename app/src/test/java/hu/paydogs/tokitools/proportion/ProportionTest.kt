package hu.paydogs.tokitools.proportion

import hu.paydogs.tokitools.proportion.ProportionField.X1
import hu.paydogs.tokitools.proportion.ProportionField.X2
import hu.paydogs.tokitools.proportion.ProportionField.Y1
import hu.paydogs.tokitools.proportion.ProportionField.Y2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProportionTest {
    // Edits fields in the given order, as a user would type them.
    private fun fill(vararg edits: Pair<ProportionField, String>,
                     separator: Char = '.',
                     decimals: Int = 4,
                     start: Proportion = Proportion.Empty): Proportion =
        edits.fold(start) { proportion: Proportion, edit: Pair<ProportionField, String> ->
            proportion.edit(edit.first, edit.second, separator, decimals)
        }

    @Test
    fun solvesEachField() {
        val y2 = fill(X1 to "2", X2 to "4", Y1 to "3")
        assertEquals("6", y2.texts[Y2])
        assertEquals(Y2, y2.result)

        val x1 = fill(X2 to "4", Y1 to "3", Y2 to "6")
        assertEquals("2", x1.texts[X1])
        assertEquals(X1, x1.result)

        val x2 = fill(X1 to "2", Y1 to "3", Y2 to "6")
        assertEquals("4", x2.texts[X2])
        assertEquals(X2, x2.result)

        val y1 = fill(X1 to "2", X2 to "4", Y2 to "6")
        assertEquals("3", y1.texts[Y1])
        assertEquals(Y1, y1.result)
    }

    @Test
    fun editingResult_makesOldestInputTheResult() {
        val solved = fill(X1 to "2", X2 to "4", Y1 to "3")

        val edited = fill(Y2 to "8", start = solved)

        assertEquals(X1, edited.result)
        assertEquals("1.5", edited.texts[X1]) // 4 · 3 / 8
        assertEquals("8", edited.texts[Y2])
    }

    @Test
    fun clearingAnInput_emptiesComputedField() {
        val solved = fill(X1 to "2", X2 to "4", Y1 to "3")

        val cleared = fill(X1 to "", start = solved)

        assertEquals("", cleared.texts[Y2])
        assertNull(cleared.result)
    }

    @Test
    fun invalidInput_noResultAndFlagged() {
        val proportion = fill(X1 to "abc", X2 to "4", Y1 to "3")

        assertTrue(proportion.isInvalid(X1))
        assertNull(proportion.result)
        assertEquals("", proportion.texts[Y2] ?: "")
    }

    @Test
    fun zeroDenominator_noResult() {
        val zeroX2 = fill(X2 to "0", X1 to "2", Y1 to "3")
        assertEquals("", zeroX2.texts[Y2])

        val zeroX1 = fill(X1 to "0", X2 to "4", Y1 to "3")
        assertEquals("", zeroX1.texts[Y2])
    }

    @Test
    fun roundsToFourDecimals_trimsZeros() {
        assertEquals("0.3333", fill(X1 to "3", X2 to "1", Y1 to "1").texts[Y2])
        assertEquals("0.5", fill(X1 to "4", X2 to "2", Y1 to "1").texts[Y2])
    }

    @Test
    fun acceptsCommaAndDot_formatsWithSeparator() {
        assertEquals("4", fill(X1 to "1,5", X2 to "3", Y1 to "2", separator = ',').texts[Y2])
        assertEquals("4", fill(X1 to "1.5", X2 to "3", Y1 to "2", separator = ',').texts[Y2])
        assertEquals("0,3333", fill(X1 to "3", X2 to "1", Y1 to "1", separator = ',').texts[Y2])
    }

    @Test
    fun roundsToGivenDecimals() {
        assertEquals("0.33", fill(X1 to "3", X2 to "1", Y1 to "1", decimals = 2).texts[Y2])
        assertEquals("0", fill(X1 to "3", X2 to "1", Y1 to "1", decimals = 0).texts[Y2])
    }

    @Test
    fun typingBeyondLimit_isRejected() {
        val proportion = fill(X1 to "1.23", X1 to "1.234", decimals = 2)

        assertEquals("1.23", proportion.texts[X1])
    }

    @Test
    fun deletingBeyondLimit_isAllowed() {
        val typed = fill(X1 to "1.2345", decimals = 4)

        val shortened = fill(X1 to "1.234", decimals = 2, start = typed)

        assertEquals("1.234", shortened.texts[X1])
    }

    @Test
    fun zeroDecimals_rejectsSeparator() {
        assertEquals("3", fill(X1 to "3", X1 to "3,", decimals = 0).texts[X1])
    }

    @Test
    fun recalculated_appliesNewDecimals() {
        val solved = fill(X1 to "3", X2 to "1", Y1 to "1")

        assertEquals("0.33", solved.recalculated('.', 2).texts[Y2])
    }

    @Test
    fun recalculated_appliesNewSeparator() {
        val solved = fill(X1 to "3", X2 to "1", Y1 to "1")

        assertEquals("0,3333", solved.recalculated(',', 4).texts[Y2])
    }

    @Test
    fun deletingTheResult_keepsItEmptyAndInputsIntact() {
        val solved = fill(X1 to "2", X2 to "4", Y1 to "3")

        val deleted = fill(Y2 to "", start = solved)

        assertEquals("", deleted.texts[Y2])
        assertNull(deleted.result)
        assertEquals("2", deleted.texts[X1])
    }

    @Test
    fun typingIntoTheResult_keepsPartialInput() {
        val solved = fill(X1 to "2", X2 to "4", Y1 to "3")

        val partial = fill(Y2 to "", Y2 to "-", start = solved)
        assertEquals("-", partial.texts[Y2])
        assertNull(partial.result)

        val negative = fill(Y2 to "-6", start = partial)
        assertEquals(X1, negative.result)
        assertEquals("-2", negative.texts[X1]) // 4 · 3 / -6
    }

    @Test
    fun exponentNotation_isInvalid() {
        val proportion = fill(X1 to "1e999999999", X2 to "4", Y1 to "3")

        assertTrue(proportion.isInvalid(X1))
        assertNull(proportion.result)
    }
}
