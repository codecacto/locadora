package br.com.codecacto.locadora.features.clientes.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para ClientesContract - State e sua lógica.
 */
class ClientesContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = ClientesContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertFalse(state.isSaving)
        assertTrue(state.clientes.isEmpty())
        assertEquals("", state.searchQuery)
        assertFalse(state.showForm)
        assertNull(state.editingCliente)
        assertEquals("", state.nomeRazao)
        assertEquals(TipoPessoa.FISICA, state.tipoPessoa)
        assertEquals("", state.cpfCnpj)
        assertEquals("", state.telefoneWhatsapp)
        assertEquals("", state.email)
        assertEquals("", state.endereco)
        assertFalse(state.precisaNotaFiscalPadrao)
        assertNull(state.emailError)
        assertNull(state.cpfCnpjError)
        assertNull(state.telefoneError)
        assertNull(state.error)
    }

    // ==================== TESTES DE filteredClientes ====================

    @Test
    fun `filteredClientes sem busca deve retornar todos os clientes`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "Maria Santos", telefoneWhatsapp = "(11) 88888-7777"),
            Cliente(id = "3", nomeRazao = "Pedro Souza", telefoneWhatsapp = "(11) 77777-6666")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = ""
        )

        assertEquals(3, state.filteredClientes.size)
        assertEquals(clientes, state.filteredClientes)
    }

    @Test
    fun `filteredClientes com busca por nome deve filtrar corretamente`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "Maria Santos", telefoneWhatsapp = "(11) 88888-7777"),
            Cliente(id = "3", nomeRazao = "Pedro Souza", telefoneWhatsapp = "(11) 77777-6666")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = "João"
        )

        assertEquals(1, state.filteredClientes.size)
        assertEquals("João Silva", state.filteredClientes.first().nomeRazao)
    }

    @Test
    fun `filteredClientes com busca case insensitive deve funcionar`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "MARIA SANTOS", telefoneWhatsapp = "(11) 88888-7777")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = "maria"
        )

        assertEquals(1, state.filteredClientes.size)
        assertEquals("MARIA SANTOS", state.filteredClientes.first().nomeRazao)
    }

    @Test
    fun `filteredClientes com busca por CPF deve filtrar corretamente`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", cpfCnpj = "12345678909", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "Maria Santos", cpfCnpj = "98765432100", telefoneWhatsapp = "(11) 88888-7777")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = "123456"
        )

        assertEquals(1, state.filteredClientes.size)
        assertEquals("João Silva", state.filteredClientes.first().nomeRazao)
    }

    @Test
    fun `filteredClientes com busca por telefone deve filtrar corretamente`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "Maria Santos", telefoneWhatsapp = "(11) 88888-7777")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = "99999"
        )

        assertEquals(1, state.filteredClientes.size)
        assertEquals("João Silva", state.filteredClientes.first().nomeRazao)
    }

    @Test
    fun `filteredClientes sem resultados deve retornar lista vazia`() {
        val clientes = listOf(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888"),
            Cliente(id = "2", nomeRazao = "Maria Santos", telefoneWhatsapp = "(11) 88888-7777")
        )

        val state = ClientesContract.State(
            clientes = clientes,
            searchQuery = "XYZ123"
        )

        assertTrue(state.filteredClientes.isEmpty())
    }

    @Test
    fun `filteredClientes com lista vazia deve retornar lista vazia`() {
        val state = ClientesContract.State(
            clientes = emptyList(),
            searchQuery = "teste"
        )

        assertTrue(state.filteredClientes.isEmpty())
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val initial = ClientesContract.State()

        val updated = initial.copy(
            isLoading = false,
            showForm = true,
            nomeRazao = "Teste"
        )

        assertFalse(updated.isLoading)
        assertTrue(updated.showForm)
        assertEquals("Teste", updated.nomeRazao)
        // Campos não alterados devem manter valores originais
        assertEquals(initial.clientes, updated.clientes)
        assertEquals(initial.tipoPessoa, updated.tipoPessoa)
    }

    @Test
    fun `State com erros de validacao deve manter erros`() {
        val state = ClientesContract.State(
            emailError = "Email inválido",
            cpfCnpjError = "CPF inválido",
            telefoneError = "Telefone inválido"
        )

        assertEquals("Email inválido", state.emailError)
        assertEquals("CPF inválido", state.cpfCnpjError)
        assertEquals("Telefone inválido", state.telefoneError)
    }

    @Test
    fun `State em modo edicao deve ter editingCliente preenchido`() {
        val cliente = Cliente(
            id = "cli-123",
            nomeRazao = "Cliente Teste",
            telefoneWhatsapp = "(11) 99999-8888"
        )

        val state = ClientesContract.State(
            showForm = true,
            editingCliente = cliente,
            nomeRazao = cliente.nomeRazao,
            telefoneWhatsapp = cliente.telefoneWhatsapp
        )

        assertTrue(state.showForm)
        assertNotNull(state.editingCliente)
        assertEquals("cli-123", state.editingCliente?.id)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action Search deve conter query correta`() {
        val action = ClientesContract.Action.Search("teste")
        assertEquals("teste", action.query)
    }

    @Test
    fun `Action SetNomeRazao deve conter valor correto`() {
        val action = ClientesContract.Action.SetNomeRazao("João")
        assertEquals("João", action.value)
    }

    @Test
    fun `Action SetTipoPessoa deve conter tipo correto`() {
        val actionFisica = ClientesContract.Action.SetTipoPessoa(TipoPessoa.FISICA)
        val actionJuridica = ClientesContract.Action.SetTipoPessoa(TipoPessoa.JURIDICA)

        assertEquals(TipoPessoa.FISICA, actionFisica.value)
        assertEquals(TipoPessoa.JURIDICA, actionJuridica.value)
    }

    @Test
    fun `Action EditCliente deve conter cliente correto`() {
        val cliente = Cliente(id = "1", nomeRazao = "Teste", telefoneWhatsapp = "(11) 99999-8888")
        val action = ClientesContract.Action.EditCliente(cliente)

        assertEquals(cliente, action.cliente)
    }

    @Test
    fun `Action DeleteCliente deve conter cliente correto`() {
        val cliente = Cliente(id = "1", nomeRazao = "Teste", telefoneWhatsapp = "(11) 99999-8888")
        val action = ClientesContract.Action.DeleteCliente(cliente)

        assertEquals(cliente, action.cliente)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = ClientesContract.Effect.ShowSuccess("Cliente salvo com sucesso!")
        assertEquals("Cliente salvo com sucesso!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = ClientesContract.Effect.ShowError("Erro ao salvar cliente")
        assertEquals("Erro ao salvar cliente", effect.message)
    }
}
