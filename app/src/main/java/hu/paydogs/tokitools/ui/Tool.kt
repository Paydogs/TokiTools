package hu.paydogs.tokitools.ui

import androidx.annotation.StringRes
import hu.paydogs.tokitools.R

enum class Tool(@StringRes val title: Int) {
    Proportion(R.string.proportion),
    Percentage(R.string.percentage),
}
