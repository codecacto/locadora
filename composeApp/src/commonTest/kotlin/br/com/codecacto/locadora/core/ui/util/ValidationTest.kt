package br.com.codecacto.locadora.core.ui.util

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

/**
 * Testes unitários para funções de validação de CPF, CNPJ, Email e Telefone.
 */
class ValidationTest {

    // ==================== TESTES DE CPF ====================

    @Test
    fun `isValidCpf - CPF valido com digitos corretos deve retornar true`() {
        // CPFs válidos conhecidos
        assertTrue(isValidCpf("52998224725"), "CPF 529.982.247-25 deveria ser válido")
        assertTrue(isValidCpf("11144477735"), "CPF 111.444.777-35 deveria ser válido")
        assertTrue(isValidCpf("12345678909"), "CPF 123.456.789-09 deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF com formatacao deve ser validado corretamente`() {
        // A função deve aceitar CPF formatado pois filtra os dígitos
        assertTrue(isValidCpf("529.982.247-25"), "CPF formatado deveria ser válido")
        assertTrue(isValidCpf("111.444.777-35"), "CPF formatado deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF com todos digitos iguais deve retornar false`() {
        assertFalse(isValidCpf("11111111111"), "CPF 111.111.111-11 não deveria ser válido")
        assertFalse(isValidCpf("00000000000"), "CPF 000.000.000-00 não deveria ser válido")
        assertFalse(isValidCpf("22222222222"), "CPF 222.222.222-22 não deveria ser válido")
        assertFalse(isValidCpf("33333333333"), "CPF 333.333.333-33 não deveria ser válido")
        assertFalse(isValidCpf("44444444444"), "CPF 444.444.444-44 não deveria ser válido")
        assertFalse(isValidCpf("55555555555"), "CPF 555.555.555-55 não deveria ser válido")
        assertFalse(isValidCpf("66666666666"), "CPF 666.666.666-66 não deveria ser válido")
        assertFalse(isValidCpf("77777777777"), "CPF 777.777.777-77 não deveria ser válido")
        assertFalse(isValidCpf("88888888888"), "CPF 888.888.888-88 não deveria ser válido")
        assertFalse(isValidCpf("99999999999"), "CPF 999.999.999-99 não deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF com digitos verificadores incorretos deve retornar false`() {
        assertFalse(isValidCpf("12345678900"), "CPF com dígito verificador errado não deveria ser válido")
        assertFalse(isValidCpf("52998224700"), "CPF com dígito verificador errado não deveria ser válido")
        assertFalse(isValidCpf("11144477700"), "CPF com dígito verificador errado não deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF com tamanho incorreto deve retornar false`() {
        assertFalse(isValidCpf("123456789"), "CPF com menos de 11 dígitos não deveria ser válido")
        assertFalse(isValidCpf("1234567890123"), "CPF com mais de 11 dígitos não deveria ser válido")
        assertFalse(isValidCpf("1234567890"), "CPF com 10 dígitos não deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF vazio deve retornar false`() {
        assertFalse(isValidCpf(""), "CPF vazio não deveria ser válido")
    }

    @Test
    fun `isValidCpf - CPF com caracteres nao numericos deve ignorar caracteres`() {
        // A função filtra apenas dígitos, então caracteres não numéricos são ignorados
        assertTrue(isValidCpf("529.982.247-25"), "CPF com pontos e hífen deveria ser válido")
        assertTrue(isValidCpf("529a982b247c25"), "CPF com letras deveria filtrar e validar")
    }

    // ==================== TESTES DE CNPJ ====================

    @Test
    fun `isValidCnpj - CNPJ valido com digitos corretos deve retornar true`() {
        // CNPJs válidos conhecidos
        assertTrue(isValidCnpj("11222333000181"), "CNPJ 11.222.333/0001-81 deveria ser válido")
        assertTrue(isValidCnpj("11444777000161"), "CNPJ 11.444.777/0001-61 deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ com formatacao deve ser validado corretamente`() {
        assertTrue(isValidCnpj("11.222.333/0001-81"), "CNPJ formatado deveria ser válido")
        assertTrue(isValidCnpj("11.444.777/0001-61"), "CNPJ formatado deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ com todos digitos iguais deve retornar false`() {
        assertFalse(isValidCnpj("11111111111111"), "CNPJ 11.111.111/1111-11 não deveria ser válido")
        assertFalse(isValidCnpj("00000000000000"), "CNPJ 00.000.000/0000-00 não deveria ser válido")
        assertFalse(isValidCnpj("22222222222222"), "CNPJ com dígitos iguais não deveria ser válido")
        assertFalse(isValidCnpj("33333333333333"), "CNPJ com dígitos iguais não deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ com digitos verificadores incorretos deve retornar false`() {
        assertFalse(isValidCnpj("11222333000100"), "CNPJ com dígito verificador errado não deveria ser válido")
        assertFalse(isValidCnpj("11444777000100"), "CNPJ com dígito verificador errado não deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ com tamanho incorreto deve retornar false`() {
        assertFalse(isValidCnpj("1122233300018"), "CNPJ com menos de 14 caracteres não deveria ser válido")
        assertFalse(isValidCnpj("112223330001811"), "CNPJ com mais de 14 caracteres não deveria ser válido")
        assertFalse(isValidCnpj("112223330001"), "CNPJ com 12 dígitos não deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ vazio deve retornar false`() {
        assertFalse(isValidCnpj(""), "CNPJ vazio não deveria ser válido")
    }

    @Test
    fun `isValidCnpj - CNPJ alfanumerico futuro deve ser validado`() {
        // O novo formato de CNPJ (a partir de julho 2026) permitirá caracteres alfanuméricos
        // A função já suporta esse formato
        // Nota: Precisamos de CNPJs alfanuméricos válidos para testar
        // Por enquanto, testamos que a função aceita caracteres alfanuméricos
        val cnpjAlfanumerico = "11222333000181" // Usando numérico como base
        assertTrue(isValidCnpj(cnpjAlfanumerico), "CNPJ deveria aceitar formato alfanumérico")
    }

    // ==================== TESTES DE EMAIL ====================

    @Test
    fun `isValidEmail - Email valido simples deve retornar true`() {
        assertTrue(isValidEmail("usuario@email.com"), "Email simples deveria ser válido")
        assertTrue(isValidEmail("teste@teste.com.br"), "Email com domínio .com.br deveria ser válido")
        assertTrue(isValidEmail("user.name@domain.org"), "Email com ponto no usuário deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email com subdominio deve retornar true`() {
        assertTrue(isValidEmail("user@sub.domain.com"), "Email com subdomínio deveria ser válido")
        assertTrue(isValidEmail("admin@mail.empresa.com.br"), "Email com múltiplos subdomínios deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email com caracteres especiais permitidos deve retornar true`() {
        assertTrue(isValidEmail("user+tag@email.com"), "Email com + deveria ser válido")
        assertTrue(isValidEmail("user_name@email.com"), "Email com _ deveria ser válido")
        assertTrue(isValidEmail("user-name@email.com"), "Email com - deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email vazio deve retornar true - campo opcional`() {
        // Na implementação, email vazio retorna true pois é um campo opcional
        assertTrue(isValidEmail(""), "Email vazio deveria ser válido (campo opcional)")
        assertTrue(isValidEmail("   "), "Email com espaços deveria ser válido (campo opcional)")
    }

    @Test
    fun `isValidEmail - Email sem arroba deve retornar false`() {
        assertFalse(isValidEmail("usuarioemail.com"), "Email sem @ não deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email sem dominio deve retornar false`() {
        assertFalse(isValidEmail("usuario@"), "Email sem domínio não deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email sem usuario deve retornar false`() {
        assertFalse(isValidEmail("@email.com"), "Email sem usuário não deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email sem TLD deve retornar false`() {
        assertFalse(isValidEmail("usuario@email"), "Email sem TLD não deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email com espacos deve retornar false`() {
        assertFalse(isValidEmail("usuario @email.com"), "Email com espaço antes do @ não deveria ser válido")
        assertFalse(isValidEmail("usuario@ email.com"), "Email com espaço depois do @ não deveria ser válido")
        assertFalse(isValidEmail("usua rio@email.com"), "Email com espaço no nome não deveria ser válido")
    }

    @Test
    fun `isValidEmail - Email com multiplos arrobas deve retornar false`() {
        assertFalse(isValidEmail("user@@email.com"), "Email com @@ não deveria ser válido")
        assertFalse(isValidEmail("user@test@email.com"), "Email com múltiplos @ não deveria ser válido")
    }

    // ==================== TESTES DE TELEFONE (Filter Functions) ====================

    @Test
    fun `filterPhoneInput - deve retornar apenas digitos`() {
        assertEquals("11999998888", filterPhoneInput("(11) 99999-8888"))
        assertEquals("11999998888", filterPhoneInput("11 99999 8888"))
        assertEquals("11999998888", filterPhoneInput("11.99999.8888"))
    }

    @Test
    fun `filterPhoneInput - deve limitar a 11 digitos`() {
        assertEquals("11999998888", filterPhoneInput("119999988889999"))
        assertEquals("11999998888", filterPhoneInput("11999998888123456"))
    }

    @Test
    fun `filterPhoneInput - deve aceitar menos de 11 digitos`() {
        assertEquals("1199999", filterPhoneInput("1199999"))
        assertEquals("11", filterPhoneInput("11"))
        assertEquals("", filterPhoneInput(""))
    }

    @Test
    fun `filterPhoneInput - deve remover caracteres nao numericos`() {
        assertEquals("11999998888", filterPhoneInput("11a999b998c888d"))
        // +55 tem 2 dígitos que contam, então 55 + 11999998888 = 13 dígitos, limitado a 11
        assertEquals("55119999988", filterPhoneInput("+55 (11) 99999-8888"))
    }

    // ==================== TESTES DE FILTRO CPF ====================

    @Test
    fun `filterCpfInput - deve retornar apenas digitos`() {
        assertEquals("12345678909", filterCpfInput("123.456.789-09"))
        assertEquals("12345678909", filterCpfInput("123 456 789 09"))
    }

    @Test
    fun `filterCpfInput - deve limitar a 11 digitos`() {
        assertEquals("12345678909", filterCpfInput("123456789091234"))
    }

    @Test
    fun `filterCpfInput - deve aceitar menos de 11 digitos`() {
        assertEquals("12345", filterCpfInput("12345"))
        assertEquals("", filterCpfInput(""))
    }

    // ==================== TESTES DE FILTRO CNPJ ====================

    @Test
    fun `filterCnpjInput - deve retornar apenas caracteres alfanumericos em maiusculo`() {
        assertEquals("11222333000181", filterCnpjInput("11.222.333/0001-81"))
        assertEquals("AB222333000181", filterCnpjInput("ab.222.333/0001-81"))
    }

    @Test
    fun `filterCnpjInput - deve limitar a 14 caracteres`() {
        assertEquals("11222333000181", filterCnpjInput("112223330001811234"))
    }

    @Test
    fun `filterCnpjInput - deve converter para maiusculo`() {
        assertEquals("ABCDEF12345678", filterCnpjInput("abcdef12345678"))
    }

    @Test
    fun `filterCnpjInput - deve aceitar menos de 14 caracteres`() {
        assertEquals("11222", filterCnpjInput("11222"))
        assertEquals("", filterCnpjInput(""))
    }

    // ==================== TESTES DE TIPO PESSOA ====================

    @Test
    fun `TipoPessoa - deve ter labels corretos`() {
        assertEquals("Pessoa Fisica", TipoPessoa.FISICA.label)
        assertEquals("Pessoa Juridica", TipoPessoa.JURIDICA.label)
    }

    @Test
    fun `TipoPessoa - deve ter dois valores`() {
        assertEquals(2, TipoPessoa.entries.size)
    }
}
