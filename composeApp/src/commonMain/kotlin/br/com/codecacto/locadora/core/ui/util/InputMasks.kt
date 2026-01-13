package br.com.codecacto.locadora.core.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Phone mask: (XX) XXXXX-XXXX (5 digits + 4 digits after area code)
 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)

        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                when (index) {
                    0 -> append("($char")
                    1 -> append("$char) ")
                    7 -> append("-$char")  // Hyphen after 5 digits (indices 2-6)
                    else -> append(char)
                }
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            PhoneOffsetMapping(digits.length)
        )
    }
}

private class PhoneOffsetMapping(private val digitsLength: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= 0 -> offset
            offset <= 2 -> offset + 1  // After (
            offset <= 7 -> offset + 3  // After ) and space
            offset <= 11 -> offset + 4 // After -
            else -> 15
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= 1 -> 0
            offset <= 4 -> offset - 1
            offset <= 11 -> offset - 3  // Updated for 5+4 format
            else -> offset - 4
        }.coerceIn(0, digitsLength)
    }
}

/**
 * CPF mask: XXX.XXX.XXX-XX
 */
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)

        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                when (index) {
                    3, 6 -> append(".$char")
                    9 -> append("-$char")
                    else -> append(char)
                }
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            CpfOffsetMapping(digits.length)
        )
    }
}

private class CpfOffsetMapping(private val digitsLength: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= 3 -> offset
            offset <= 6 -> offset + 1
            offset <= 9 -> offset + 2
            offset <= 11 -> offset + 3
            else -> 14
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= 3 -> offset
            offset <= 7 -> offset - 1
            offset <= 11 -> offset - 2
            else -> offset - 3
        }.coerceIn(0, digitsLength)
    }
}

/**
 * CNPJ mask: XX.XXX.XXX/XXXX-XX
 * Also supports new alphanumeric format
 */
class CnpjVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Allow alphanumeric for new CNPJ format
        val chars = text.text.filter { it.isLetterOrDigit() }.uppercase().take(14)

        if (chars.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formatted = buildString {
            chars.forEachIndexed { index, char ->
                when (index) {
                    2, 5 -> append(".$char")
                    8 -> append("/$char")
                    12 -> append("-$char")
                    else -> append(char)
                }
            }
        }

        return TransformedText(
            AnnotatedString(formatted),
            CnpjOffsetMapping(chars.length)
        )
    }
}

private class CnpjOffsetMapping(private val charsLength: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= 2 -> offset
            offset <= 5 -> offset + 1
            offset <= 8 -> offset + 2
            offset <= 12 -> offset + 3
            offset <= 14 -> offset + 4
            else -> 18
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= 2 -> offset
            offset <= 6 -> offset - 1
            offset <= 10 -> offset - 2
            offset <= 15 -> offset - 3
            else -> offset - 4
        }.coerceIn(0, charsLength)
    }
}

/**
 * Filter phone input - only digits, max 11
 */
fun filterPhoneInput(input: String): String {
    return input.filter { it.isDigit() }.take(11)
}

/**
 * Filter CPF input - only digits, max 11
 */
fun filterCpfInput(input: String): String {
    return input.filter { it.isDigit() }.take(11)
}

/**
 * Filter CNPJ input - alphanumeric, max 14
 */
fun filterCnpjInput(input: String): String {
    return input.filter { it.isLetterOrDigit() }.uppercase().take(14)
}

/**
 * Validate email format
 */
fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return true // Empty is valid (optional field)
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

/**
 * Validate CPF (basic validation - 11 digits)
 */
fun isValidCpf(cpf: String): Boolean {
    val digits = cpf.filter { it.isDigit() }
    if (digits.length != 11) return false

    // Check if all digits are the same
    if (digits.all { it == digits[0] }) return false

    // Validate check digits
    val numbers = digits.map { it.toString().toInt() }

    // First check digit
    var sum = 0
    for (i in 0..8) {
        sum += numbers[i] * (10 - i)
    }
    var remainder = sum % 11
    val firstCheckDigit = if (remainder < 2) 0 else 11 - remainder
    if (numbers[9] != firstCheckDigit) return false

    // Second check digit
    sum = 0
    for (i in 0..9) {
        sum += numbers[i] * (11 - i)
    }
    remainder = sum % 11
    val secondCheckDigit = if (remainder < 2) 0 else 11 - remainder
    return numbers[10] == secondCheckDigit
}

/**
 * Validate CNPJ with check digit verification
 * Supports both numeric and new alphanumeric format (starting July 2026)
 * Uses ASCII code - 48 for conversion: 0-9 = 0-9, A=17, B=18, C=19, etc.
 */
fun isValidCnpj(cnpj: String): Boolean {
    val chars = cnpj.filter { it.isLetterOrDigit() }.uppercase()
    if (chars.length != 14) return false

    // Check if all chars are the same
    if (chars.all { it == chars[0] }) return false

    // Convert chars to values using ASCII code - 48
    // This way: 0-9 stay as 0-9, and A=17, B=18, C=19, etc.
    val values = chars.map { char ->
        char.code - 48
    }

    // First check digit calculation (Módulo 11)
    val weights1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    var sum = 0
    for (i in 0..11) {
        sum += values[i] * weights1[i]
    }
    var remainder = sum % 11
    val firstCheckDigit = if (remainder < 2) 0 else 11 - remainder
    if (values[12] != firstCheckDigit) return false

    // Second check digit calculation (Módulo 11)
    val weights2 = listOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    sum = 0
    for (i in 0..12) {
        sum += values[i] * weights2[i]
    }
    remainder = sum % 11
    val secondCheckDigit = if (remainder < 2) 0 else 11 - remainder
    return values[13] == secondCheckDigit
}

/**
 * Enum for person type
 */
enum class TipoPessoa(val label: String) {
    FISICA("Pessoa Fisica"),
    JURIDICA("Pessoa Juridica")
}
