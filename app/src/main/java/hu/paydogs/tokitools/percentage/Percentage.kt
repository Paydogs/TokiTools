package hu.paydogs.tokitools.percentage

import hu.paydogs.tokitools.calc.formatDecimal
import hu.paydogs.tokitools.calc.parseDecimal
import hu.paydogs.tokitools.calc.withinDecimals
import java.math.BigDecimal
import java.math.RoundingMode

enum class PercentageField { X, Y, Z }

/**
 * Y% of X is Z. Same rules as the proportion: the two most recently typed valid fields are the
 * inputs; the third is computed into [texts] and named by [result]. [typed] is oldest first.
 */
data class Percentage(val texts: Map<PercentageField, String>,
                      val typed: List<PercentageField>,
                      val result: PercentageField?) {

    fun edit(field: PercentageField, text: String, decimalSeparator: Char, decimals: Int): Percentage {
        // Only growth is blocked, so shortening an over-long value after lowering the limit works.
        val previous = texts[field].orEmpty()
        if (text.length > previous.length && !withinDecimals(text, decimals)) return this
        return resolve(texts = texts + (field to text),
                       typed = typed - field + field,
                       edited = field,
                       decimalSeparator = decimalSeparator,
                       decimals = decimals)
    }

    /** Re-solves with the current inputs, e.g. after the decimal limit or language changed. */
    fun recalculated(decimalSeparator: Char, decimals: Int): Percentage =
        resolve(texts = texts,
                typed = typed,
                edited = null,
                decimalSeparator = decimalSeparator,
                decimals = decimals)

    fun isInvalid(field: PercentageField): Boolean {
        val text = texts[field].orEmpty()
        return text.isNotBlank() && parseDecimal(text) == null
    }

    private fun resolve(texts: Map<PercentageField, String>,
                        typed: List<PercentageField>,
                        edited: PercentageField?,
                        decimalSeparator: Char,
                        decimals: Int): Percentage {
        val inputs = typed.asReversed()
            .filter { candidate: PercentageField -> parseDecimal(texts[candidate]) != null }
            .take(2)

        if (inputs.size < 2) {
            // A value computed from inputs that no longer exist must not linger.
            val stale = result?.takeIf { previous: PercentageField -> previous != edited }
            val cleaned = if (stale == null) texts else texts + (stale to "")
            return Percentage(texts = cleaned, typed = typed, result = null)
        }

        val target = PercentageField.entries.first { candidate: PercentageField -> candidate !in inputs }
        // The user is clearing or starting to type into this field; don't overwrite their text.
        if (target == edited) return Percentage(texts = texts, typed = typed, result = null)
        val values = inputs.associateWith { input: PercentageField -> parseDecimal(texts[input])!! }
        val solved = solve(target, values, decimals)?.let { value: BigDecimal -> formatDecimal(value, decimalSeparator) }
        return Percentage(texts = texts + (target to (solved ?: "")),
                          typed = typed - target,
                          result = target)
    }

    companion object {
        val Empty = Percentage(texts = emptyMap(), typed = emptyList(), result = null)

        private val Hundred = BigDecimal(100)

        // Null when the answer would need dividing by zero.
        private fun solve(target: PercentageField,
                          values: Map<PercentageField, BigDecimal>,
                          decimals: Int): BigDecimal? {
            fun v(field: PercentageField): BigDecimal = values.getValue(field)
            val (numerator: BigDecimal, divisor: BigDecimal) = when (target) {
                PercentageField.Z -> v(PercentageField.X) * v(PercentageField.Y) to Hundred
                PercentageField.X -> v(PercentageField.Z) * Hundred to v(PercentageField.Y)
                PercentageField.Y -> v(PercentageField.Z) * Hundred to v(PercentageField.X)
            }
            if (divisor.signum() == 0) return null
            return numerator.divide(divisor, decimals, RoundingMode.HALF_UP)
        }
    }
}
