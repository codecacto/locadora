package br.com.codecacto.locadora.core.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Testes unitários para funções de formatação e conversão de moeda.
 */
class CurrencyUtilTest {

    // ==================== TESTES DE CONVERSAO CURRENCY TO DOUBLE ====================

    @Test
    fun `currencyToDouble - valor formatado com milhares deve converter corretamente`() {
        assertEquals(1500.0, "1.500,00".currencyToDouble(), 0.001)
        assertEquals(10000.0, "10.000,00".currencyToDouble(), 0.001)
        assertEquals(1000000.0, "1.000.000,00".currencyToDouble(), 0.001)
    }

    @Test
    fun `currencyToDouble - valor com centavos deve converter corretamente`() {
        assertEquals(99.99, "99,99".currencyToDouble(), 0.001)
        assertEquals(0.01, "0,01".currencyToDouble(), 0.001)
        assertEquals(0.50, "0,50".currencyToDouble(), 0.001)
        assertEquals(123.45, "123,45".currencyToDouble(), 0.001)
    }

    @Test
    fun `currencyToDouble - valor zero deve retornar zero`() {
        assertEquals(0.0, "0,00".currencyToDouble(), 0.001)
        assertEquals(0.0, "".currencyToDouble(), 0.001)
    }

    @Test
    fun `currencyToDouble - valor apenas digitos deve converter corretamente`() {
        // Sem formatação, os últimos 2 dígitos são centavos
        assertEquals(15.00, "1500".currencyToDouble(), 0.001)
        assertEquals(1.23, "123".currencyToDouble(), 0.001)
        assertEquals(0.99, "99".currencyToDouble(), 0.001)
        assertEquals(0.09, "9".currencyToDouble(), 0.001)
    }

    @Test
    fun `currencyToDouble - valor com prefixo RS deve converter corretamente`() {
        // A função deve ignorar caracteres não numéricos
        assertEquals(100.0, "R$ 100,00".currencyToDouble(), 0.001)
        assertEquals(1500.0, "R$ 1.500,00".currencyToDouble(), 0.001)
    }

    @Test
    fun `currencyToDouble - valor grande deve converter corretamente`() {
        assertEquals(999999.99, "999.999,99".currencyToDouble(), 0.001)
        assertEquals(1234567.89, "1.234.567,89".currencyToDouble(), 0.001)
    }

    // ==================== TESTES DE FILTRO CURRENCY INPUT ====================

    @Test
    fun `filterCurrencyInput - deve retornar apenas digitos`() {
        assertEquals("10000", filterCurrencyInput("100,00"))
        assertEquals("150000", filterCurrencyInput("1.500,00"))
        assertEquals("100000000", filterCurrencyInput("1.000.000,00"))
    }

    @Test
    fun `filterCurrencyInput - deve remover prefixo RS`() {
        assertEquals("10000", filterCurrencyInput("R$ 100,00"))
        assertEquals("150000", filterCurrencyInput("R$ 1.500,00"))
    }

    @Test
    fun `filterCurrencyInput - valor vazio deve retornar vazio`() {
        assertEquals("", filterCurrencyInput(""))
        assertEquals("", filterCurrencyInput("R$ "))
    }

    @Test
    fun `filterCurrencyInput - deve aceitar apenas digitos de entrada`() {
        assertEquals("12345", filterCurrencyInput("12345"))
        assertEquals("0", filterCurrencyInput("0"))
    }

    @Test
    fun `filterCurrencyInput - deve remover espacos e caracteres especiais`() {
        assertEquals("12345", filterCurrencyInput(" 1 2 3 4 5 "))
        assertEquals("12345", filterCurrencyInput("1a2b3c4d5e"))
    }
}
