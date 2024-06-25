package uk.nottsknight.basicdailybudget.ui

import android.icu.text.DecimalFormat
import android.icu.util.Currency
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


class CurrencyVisualTransformation() : VisualTransformation {
    private val format = DecimalFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 2
    }

    private val validChars = "^[0-9]+\\.[0-9]+$".toPattern()

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text.trim()

        if (original.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        if (!original.matches(validChars.toRegex())) {
            Log.w("BasicDailyBudget", "Currency transform requires only numberical input")
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = format.format(original.toDouble())
        return TransformedText(
            AnnotatedString(formatted),
            CurrencyOffsetMapping(original, formatted)
        )
    }
}

private class CurrencyOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {
    private val offsetList: List<Int> = mutableListOf<Int>().apply {
        var pos = 0
        for (c in original) {
            val idx = formatted.indexOf(c, pos)
            if (idx != -1) {
                add(idx)
                pos++
            } else {
                clear()
                return@apply
            }
        }
    }

    override fun originalToTransformed(offset: Int) =
        if (offset >= original.length) {
            formatted.length
        } else {
            offset + 1
        }

    override fun transformedToOriginal(offset: Int) =
        if (offset == 0) {
            0
        } else {
            offset - 1
        }
}