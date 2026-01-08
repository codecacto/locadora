package br.com.codecacto.locadora.core.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CurrencyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text

        // Remove any non-digit characters
        val digits = originalText.filter { it.isDigit() }

        if (digits.isEmpty()) {
            return TransformedText(
                AnnotatedString(""),
                OffsetMapping.Identity
            )
        }

        // Format the currency
        val formatted = formatCurrency(digits)

        return TransformedText(
            AnnotatedString(formatted),
            CurrencyOffsetMapping(originalText, formatted)
        )
    }

    private fun formatCurrency(digits: String): String {
        // Pad with zeros if less than 3 digits (to have at least 0,XX)
        val paddedDigits = digits.padStart(3, '0')

        // Split into integer and decimal parts
        val decimalPart = paddedDigits.takeLast(2)
        val integerPart = paddedDigits.dropLast(2).toLongOrNull() ?: 0L

        // Format integer part with thousand separators
        val formattedInteger = formatWithThousandSeparator(integerPart)

        return "$formattedInteger,$decimalPart"
    }

    private fun formatWithThousandSeparator(value: Long): String {
        val str = value.toString()
        val result = StringBuilder()

        str.reversed().forEachIndexed { index, char ->
            if (index > 0 && index % 3 == 0) {
                result.append('.')
            }
            result.append(char)
        }

        return result.reverse().toString()
    }
}

private class CurrencyOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        // Map cursor position from original (digits only) to formatted text
        return formatted.length.coerceAtMost(offset + (formatted.length - original.filter { it.isDigit() }.length).coerceAtLeast(0))
    }

    override fun transformedToOriginal(offset: Int): Int {
        // Map cursor position from formatted text back to original
        return original.filter { it.isDigit() }.length.coerceAtMost(offset)
    }
}

/**
 * Converts a formatted currency string (e.g., "1.000,00") to a Double value
 */
fun String.currencyToDouble(): Double {
    val digits = this.filter { it.isDigit() }
    if (digits.isEmpty()) return 0.0

    val paddedDigits = digits.padStart(3, '0')
    val decimalPart = paddedDigits.takeLast(2)
    val integerPart = paddedDigits.dropLast(2)

    return "$integerPart.$decimalPart".toDoubleOrNull() ?: 0.0
}

/**
 * Filters input to only allow digits
 */
fun filterCurrencyInput(input: String): String {
    return input.filter { it.isDigit() }
}
