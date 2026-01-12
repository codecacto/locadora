package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Testes unitários para a classe Cliente.
 */
class ClienteTest {

    // ==================== TESTES DE Cliente - Valores Padrao ====================

    @Test
    fun `Cliente com valores padrao deve ter campos corretos`() {
        val cliente = Cliente()

        assertEquals("", cliente.id)
        assertEquals("", cliente.nomeRazao)
        assertNull(cliente.cpfCnpj)
        assertEquals("", cliente.telefoneWhatsapp)
        assertNull(cliente.email)
        assertNull(cliente.endereco)
        assertFalse(cliente.precisaNotaFiscalPadrao)
    }

    @Test
    fun `Cliente COLLECTION_NAME deve ser correto`() {
        assertEquals("clientes", Cliente.COLLECTION_NAME)
    }

    // ==================== TESTES DE Cliente - Criacao ====================

    @Test
    fun `Cliente pessoa fisica deve ser criado corretamente`() {
        val cliente = Cliente(
            id = "cli-123",
            nomeRazao = "João da Silva",
            cpfCnpj = "12345678909",
            telefoneWhatsapp = "(11) 99999-8888",
            email = "joao@email.com",
            endereco = "Rua das Flores, 123",
            precisaNotaFiscalPadrao = false
        )

        assertEquals("cli-123", cliente.id)
        assertEquals("João da Silva", cliente.nomeRazao)
        assertEquals("12345678909", cliente.cpfCnpj)
        assertEquals("(11) 99999-8888", cliente.telefoneWhatsapp)
        assertEquals("joao@email.com", cliente.email)
        assertEquals("Rua das Flores, 123", cliente.endereco)
        assertFalse(cliente.precisaNotaFiscalPadrao)
    }

    @Test
    fun `Cliente pessoa juridica deve ser criado corretamente`() {
        val cliente = Cliente(
            id = "cli-456",
            nomeRazao = "Empresa LTDA",
            cpfCnpj = "11222333000181",
            telefoneWhatsapp = "(11) 3333-4444",
            email = "contato@empresa.com.br",
            endereco = "Av. Industrial, 1000",
            precisaNotaFiscalPadrao = true
        )

        assertEquals("cli-456", cliente.id)
        assertEquals("Empresa LTDA", cliente.nomeRazao)
        assertEquals("11222333000181", cliente.cpfCnpj)
        assertEquals("(11) 3333-4444", cliente.telefoneWhatsapp)
        assertEquals("contato@empresa.com.br", cliente.email)
        assertEquals("Av. Industrial, 1000", cliente.endereco)
        assertTrue(cliente.precisaNotaFiscalPadrao)
    }

    // ==================== TESTES DE Cliente - Campos Opcionais ====================

    @Test
    fun `Cliente sem email deve ter email null`() {
        val cliente = Cliente(
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 99999-8888",
            email = null
        )

        assertNull(cliente.email)
    }

    @Test
    fun `Cliente sem endereco deve ter endereco null`() {
        val cliente = Cliente(
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 99999-8888",
            endereco = null
        )

        assertNull(cliente.endereco)
    }

    @Test
    fun `Cliente sem CPF CNPJ deve ter cpfCnpj null`() {
        val cliente = Cliente(
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 99999-8888",
            cpfCnpj = null
        )

        assertNull(cliente.cpfCnpj)
    }

    // ==================== TESTES DE Cliente - Nota Fiscal ====================

    @Test
    fun `Cliente que precisa de nota fiscal deve ter flag true`() {
        val cliente = Cliente(
            nomeRazao = "Empresa LTDA",
            telefoneWhatsapp = "(11) 3333-4444",
            precisaNotaFiscalPadrao = true
        )

        assertTrue(cliente.precisaNotaFiscalPadrao)
    }

    @Test
    fun `Cliente que nao precisa de nota fiscal deve ter flag false`() {
        val cliente = Cliente(
            nomeRazao = "Pessoa Física",
            telefoneWhatsapp = "(11) 99999-8888",
            precisaNotaFiscalPadrao = false
        )

        assertFalse(cliente.precisaNotaFiscalPadrao)
    }

    // ==================== TESTES DE Cliente - Copy ====================

    @Test
    fun `Cliente copy deve criar nova instancia com valores alterados`() {
        val original = Cliente(
            id = "cli-123",
            nomeRazao = "Nome Original",
            telefoneWhatsapp = "(11) 99999-8888",
            email = "original@email.com"
        )

        val atualizado = original.copy(
            nomeRazao = "Nome Atualizado",
            email = "atualizado@email.com"
        )

        // Original deve permanecer inalterado
        assertEquals("Nome Original", original.nomeRazao)
        assertEquals("original@email.com", original.email)

        // Cópia deve ter valores atualizados
        assertEquals("Nome Atualizado", atualizado.nomeRazao)
        assertEquals("atualizado@email.com", atualizado.email)

        // Valores não alterados devem ser mantidos
        assertEquals(original.id, atualizado.id)
        assertEquals(original.telefoneWhatsapp, atualizado.telefoneWhatsapp)
    }

    // ==================== TESTES DE Cliente - Telefone ====================

    @Test
    fun `Cliente com telefone celular formatado`() {
        val cliente = Cliente(
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 99999-8888"
        )

        assertEquals("(11) 99999-8888", cliente.telefoneWhatsapp)
    }

    @Test
    fun `Cliente com telefone fixo formatado`() {
        val cliente = Cliente(
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 3333-4444"
        )

        assertEquals("(11) 3333-4444", cliente.telefoneWhatsapp)
    }

    // ==================== TESTES DE Cliente - Timestamps ====================

    @Test
    fun `Cliente deve ter timestamps de criacao e atualizacao`() {
        val antes = System.currentTimeMillis()
        val cliente = Cliente(nomeRazao = "Teste")
        val depois = System.currentTimeMillis()

        assertTrue(cliente.criadoEm >= antes)
        assertTrue(cliente.criadoEm <= depois)
        assertTrue(cliente.atualizadoEm >= antes)
        assertTrue(cliente.atualizadoEm <= depois)
    }

    // ==================== TESTES DE Cliente - Tipo Pessoa (via tamanho CPF/CNPJ) ====================

    @Test
    fun `Cliente com CPF 11 digitos indica pessoa fisica`() {
        val cliente = Cliente(
            nomeRazao = "Pessoa Física",
            cpfCnpj = "12345678909"
        )

        // CPF tem 11 dígitos
        assertEquals(11, cliente.cpfCnpj?.length)
    }

    @Test
    fun `Cliente com CNPJ 14 digitos indica pessoa juridica`() {
        val cliente = Cliente(
            nomeRazao = "Pessoa Jurídica",
            cpfCnpj = "11222333000181"
        )

        // CNPJ tem 14 dígitos
        assertEquals(14, cliente.cpfCnpj?.length)
    }
}
