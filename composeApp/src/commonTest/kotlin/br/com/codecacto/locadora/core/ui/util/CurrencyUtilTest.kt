package br.com.codecacto.locadora.core.ui.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Testes unitários para funções de formatação e conversão de moeda.
 */
class CurrencyUtilTest {

    // ==================== TESTES DE FORMATACAO COM SEPARADOR DE MILHARES ====================

    @Test
    fun `formatAsCurrency - valor inteiro deve formatar com separador de milhares`() {
        assertEquals("R$ 1.000,00", 1000.0.formatAsCurrency())
        assertEquals("R$ 10.000,00", 10000.0.formatAsCurrency())
        assertEquals("R$ 100.000,00", 100000.0.formatAsCurrency())
        assertEquals("R$ 1.000.000,00", 1000000.0.formatAsCurrency())
    }

    @Test
    fun `formatAsCurrency - valor com centavos deve formatar corretamente`() {
        assertEquals("R$ 1.234,56", 1234.56.formatAsCurrency())
        assertEquals("R$ 99,99", 99.99.formatAsCurrency())
        assertEquals("R$ 1.500,50", 1500.50.formatAsCurrency())
    }

    @Test
    fun `formatAsCurrency - valor pequeno sem separador de milhares`() {
        assertEquals("R$ 100,00", 100.0.formatAsCurrency())
        assertEquals("R$ 999,00", 999.0.formatAsCurrency())
        assertEquals("R$ 0,00", 0.0.formatAsCurrency())
    }

    @Test
    fun `formatAsCurrency - valor negativo deve mostrar sinal de menos`() {
        assertEquals("-R$ 1.000,00", (-1000.0).formatAsCurrency())
        assertEquals("-R$ 500,00", (-500.0).formatAsCurrency())
        assertEquals("-R$ 1.234,56", (-1234.56).formatAsCurrency())
    }

    @Test
    fun `formatAsCurrency - valores grandes devem formatar corretamente`() {
        assertEquals("R$ 999.999,99", 999999.99.formatAsCurrency())
        assertEquals("R$ 1.234.567,89", 1234567.89.formatAsCurrency())
        assertEquals("R$ 10.000.000,00", 10000000.0.formatAsCurrency())
    }

    @Test
    fun `formatAsCurrency - centavos devem ter dois digitos`() {
        assertEquals("R$ 100,00", 100.0.formatAsCurrency())
        assertEquals("R$ 100,01", 100.01.formatAsCurrency())
        assertEquals("R$ 100,10", 100.10.formatAsCurrency())
    }

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
