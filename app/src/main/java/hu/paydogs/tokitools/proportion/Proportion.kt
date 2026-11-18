package hu.paydogs.tokitools.proportion

import hu.paydogs.tokitools.calc.formatDecimal
import hu.paydogs.tokitools.calc.parseDecimal
import hu.paydogs.tokitools.calc.withinDecimals
import java.math.BigDecimal
import java.math.RoundingMode

enum class ProportionField { X1, X2, Y1, Y2 }

/**
 * X1 / X2 = Y1 / Y2. The three most recently typed valid fields are the inputs; the fourth is
 * computed into [texts] and named by [result]. [typed] holds user-typed fields, oldest first.
 */
data class Proportion(val texts: Map<ProportionField, String>,
                      val typed: List<ProportionField>,
                      val result: ProportionField?) {

    fun edit(field: ProportionField, text: String, decimalSeparator: Char, decimals: Int): Proportion {
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
    fun recalculated(decimalSeparator: Char, decimals: Int): Proportion =
        resolve(texts = texts,
                typed = typed,
                edited = null,
                decimalSeparator = decimalSeparator,
                decimals = decimals)

    fun isInvalid(field: ProportionField): Boolean {
        val text = texts[field].orEmpty()
        return text.isNotBlank() && parseDecimal(text) == null
    }

    /** The current values when all four are filled (a result was calculated), else null. */
    fun remembered(): RememberedProportion? {
        val calculated = result ?: return null
        if (texts[calculated].isNullOrBlank()) return null
        return RememberedProportion(values = ProportionField.entries.associateWith { field: ProportionField ->
                                        texts[field].orEmpty()
                                    },
                                    result = calculated)
    }

    private fun resolve(texts: Map<ProportionField, String>,
                        typed: List<ProportionField>,
                        edited: ProportionField?,
                        decimalSeparator: Char,
                        decimals: Int): Proportion {
        val inputs = typed.asReversed()
            .filter { candidate: ProportionField -> parseDecimal(texts[candidate]) != null }
            .take(3)

        if (inputs.size < 3) {
            // A value computed from inputs that no longer exist must not linger.
            val stale = result?.takeIf { previous: ProportionField -> previous != edited }
            val cleaned = if (stale == null) texts else texts + (stale to "")
            return Proportion(texts = cleaned, typed = typed, result = null)
        }

        val target = ProportionField.entries.first { candidate: ProportionField -> candidate !in inputs }
        // The user is clearing or starting to type into this field; don't overwrite their text.
        if (target == edited) return Proportion(texts = texts, typed = typed, result = null)
        val values = inputs.associateWith { input: ProportionField -> parseDecimal(texts[input])!! }
        val solved = solve(target, values, decimals)?.let { value: BigDecimal -> formatDecimal(value, decimalSeparator) }
        return Proportion(texts = texts + (target to (solved ?: "")),
                          typed = typed - target,
                          result = target)
    }

    companion object {
        val Empty = Proportion(texts = emptyMap(), typed = emptyList(), result = null)

        /** Puts a remembered proportion back, with the same field marked as calculated. */
        fun restored(entry: RememberedProportion): Proportion =
            Proportion(texts = entry.values,
                       typed = ProportionField.entries - entry.result,
                       result = entry.result)

        private val denominators = setOf(ProportionField.X2, ProportionField.Y2)

        // Null when a fraction would have a zero denominator.
        private fun solve(target: ProportionField,
                          values: Map<ProportionField, BigDecimal>,
                          decimals: Int): BigDecimal? {
            if (values.any { (field: ProportionField, value: BigDecimal) -> field in denominators && value.signum() == 0 }) {
                return null
            }
            fun v(field: ProportionField): BigDecimal = values.getValue(field)
            val (numerator: BigDecimal, divisor: BigDecimal) = when (target) {
                ProportionField.X1 -> v(ProportionField.X2) * v(ProportionField.Y1) to v(ProportionField.Y2)
                ProportionField.X2 -> v(ProportionField.X1) * v(ProportionField.Y2) to v(ProportionField.Y1)
                ProportionField.Y1 -> v(ProportionField.X1) * v(ProportionField.Y2) to v(ProportionField.X2)
                ProportionField.Y2 -> v(ProportionField.X2) * v(ProportionField.Y1) to v(ProportionField.X1)
            }
            if (divisor.signum() == 0) return null
            // Exact zero only: a non-zero value rounded to 0 is still a valid denominator.
            if (target in denominators && numerator.signum() == 0) return null
            return numerator.divide(divisor, decimals, RoundingMode.HALF_UP)
        }
    }
}
