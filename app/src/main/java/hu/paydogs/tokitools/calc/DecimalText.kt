package hu.paydogs.tokitools.calc

import java.math.BigDecimal

// Plain decimals only: exponent notation like "1e999999999" would overflow BigDecimal.
private val PlainNumber = Regex("""[+-]?(\d+([.,]\d*)?|[.,]\d+)""")

/** Accepts both `,` and `.` as the decimal separator; null for anything but a plain decimal. */
internal fun parseDecimal(text: String?): BigDecimal? {
    val trimmed = text?.trim() ?: return null
    if (!PlainNumber.matches(trimmed)) return null
    return trimmed.replace(',', '.').toBigDecimal()
}

internal fun withinDecimals(text: String, decimals: Int): Boolean {
    val separatorIndex = text.indexOfFirst { character: Char -> character == ',' || character == '.' }
    if (separatorIndex < 0) return true
    return decimals > 0 && text.length - separatorIndex - 1 <= decimals
}

internal fun formatDecimal(value: BigDecimal, decimalSeparator: Char): String =
    value.stripTrailingZeros().toPlainString().replace('.', decimalSeparator)
