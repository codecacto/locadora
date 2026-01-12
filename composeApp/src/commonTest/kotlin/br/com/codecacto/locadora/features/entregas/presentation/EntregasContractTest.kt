package br.com.codecacto.locadora.features.entregas.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para EntregasContract - State, Actions e Effects.
 */
class EntregasContractTest {

    // ==================== TESTES DE EntregaComDetalhes ====================

    @Test
    fun `EntregaComDetalhes com todos os dados`() {
        val locacao = Locacao(id = "loc-123", clienteId = "cli-456", equipamentoId = "eq-789")
        val cliente = Cliente(id = "cli-456", nomeRazao = "João Silva")
        val equipamento = Equipamento(id = "eq-789", nome = "Betoneira 400L")

        val entrega = EntregaComDetalhes(
            locacao = locacao,
            cliente = cliente,
            equipamento = equipamento,
            isAtrasada = true,
            isHoje = false
        )

        assertEquals("loc-123", entrega.locacao.id)
        assertEquals("João Silva", entrega.cliente?.nomeRazao)
        assertEquals("Betoneira 400L", entrega.equipamento?.nome)
        assertTrue(entrega.isAtrasada)
        assertFalse(entrega.isHoje)
    }

    @Test
    fun `EntregaComDetalhes com cliente nulo`() {
        val locacao = Locacao(id = "loc-123")

        val entrega = EntregaComDetalhes(
            locacao = locacao,
            cliente = null,
            equipamento = Equipamento(id = "eq-789", nome = "Andaime"),
            isAtrasada = false,
            isHoje = true
        )

        assertNull(entrega.cliente)
        assertNotNull(entrega.equipamento)
        assertTrue(entrega.isHoje)
    }

    @Test
    fun `EntregaComDetalhes com equipamento nulo`() {
        val locacao = Locacao(id = "loc-123")

        val entrega = EntregaComDetalhes(
            locacao = locacao,
            cliente = Cliente(id = "cli-456", nomeRazao = "João"),
            equipamento = null,
            isAtrasada = false,
            isHoje = false
        )

        assertNotNull(entrega.cliente)
        assertNull(entrega.equipamento)
    }

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = EntregasContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.entregasAtrasadas.isEmpty())
        assertTrue(state.entregasHoje.isEmpty())
        assertTrue(state.entregasAgendadas.isEmpty())
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Listas de Entregas ====================

    @Test
    fun `State com entregas atrasadas`() {
        val entregas = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-1"),
                cliente = Cliente(id = "cli-1", nomeRazao = "João"),
                equipamento = Equipamento(id = "eq-1", nome = "Betoneira"),
                isAtrasada = true,
                isHoje = false
            ),
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-2"),
                cliente = Cliente(id = "cli-2", nomeRazao = "Maria"),
                equipamento = Equipamento(id = "eq-2", nome = "Andaime"),
                isAtrasada = true,
                isHoje = false
            )
        )

        val state = EntregasContract.State(entregasAtrasadas = entregas)

        assertEquals(2, state.entregasAtrasadas.size)
        assertTrue(state.entregasAtrasadas.all { it.isAtrasada })
    }

    @Test
    fun `State com entregas de hoje`() {
        val entregas = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-1"),
                cliente = Cliente(id = "cli-1", nomeRazao = "Pedro"),
                equipamento = Equipamento(id = "eq-1", nome = "Furadeira"),
                isAtrasada = false,
                isHoje = true
            )
        )

        val state = EntregasContract.State(entregasHoje = entregas)

        assertEquals(1, state.entregasHoje.size)
        assertTrue(state.entregasHoje.all { it.isHoje })
    }

    @Test
    fun `State com entregas agendadas`() {
        val entregas = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-1"),
                cliente = Cliente(id = "cli-1", nomeRazao = "Ana"),
                equipamento = Equipamento(id = "eq-1", nome = "Compressor"),
                isAtrasada = false,
                isHoje = false
            )
        )

        val state = EntregasContract.State(entregasAgendadas = entregas)

        assertEquals(1, state.entregasAgendadas.size)
        assertFalse(state.entregasAgendadas.first().isAtrasada)
        assertFalse(state.entregasAgendadas.first().isHoje)
    }

    // ==================== TESTES DE STATE - Loading e Refresh ====================

    @Test
    fun `State durante loading`() {
        val state = EntregasContract.State(isLoading = true)

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `State durante refresh`() {
        val state = EntregasContract.State(
            isLoading = false,
            isRefreshing = true
        )

        assertFalse(state.isLoading)
        assertTrue(state.isRefreshing)
    }

    @Test
    fun `State com erro`() {
        val state = EntregasContract.State(error = "Erro ao carregar entregas")

        assertEquals("Erro ao carregar entregas", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action MarcarEntregue deve conter locacaoId correto`() {
        val action = EntregasContract.Action.MarcarEntregue("loc-123")

        assertEquals("loc-123", action.locacaoId)
    }

    @Test
    fun `Action SelectLocacao deve conter locacao correta`() {
        val locacao = Locacao(
            id = "loc-123",
            clienteId = "cli-456",
            statusEntrega = StatusEntrega.AGENDADA
        )
        val action = EntregasContract.Action.SelectLocacao(locacao)

        assertEquals("loc-123", action.locacao.id)
        assertEquals("cli-456", action.locacao.clienteId)
        assertEquals(StatusEntrega.AGENDADA, action.locacao.statusEntrega)
    }

    @Test
    fun `Action Refresh deve existir`() {
        val action = EntregasContract.Action.Refresh
        assertTrue(action is EntregasContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateToDetalhes deve conter locacaoId correto`() {
        val effect = EntregasContract.Effect.NavigateToDetalhes("loc-123")

        assertEquals("loc-123", effect.locacaoId)
    }

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = EntregasContract.Effect.ShowSuccess("Entrega marcada!")

        assertEquals("Entrega marcada!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = EntregasContract.Effect.ShowError("Erro ao marcar entrega")

        assertEquals("Erro ao marcar entrega", effect.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - carregar lista de entregas`() {
        var state = EntregasContract.State()

        // Inicia loading
        assertTrue(state.isLoading)

        // Dados carregados
        val atrasadas = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-1"),
                cliente = Cliente(id = "cli-1", nomeRazao = "João"),
                equipamento = Equipamento(id = "eq-1", nome = "Betoneira"),
                isAtrasada = true,
                isHoje = false
            )
        )
        val hoje = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-2"),
                cliente = Cliente(id = "cli-2", nomeRazao = "Maria"),
                equipamento = Equipamento(id = "eq-2", nome = "Andaime"),
                isAtrasada = false,
                isHoje = true
            )
        )
        val agendadas = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-3"),
                cliente = Cliente(id = "cli-3", nomeRazao = "Pedro"),
                equipamento = Equipamento(id = "eq-3", nome = "Furadeira"),
                isAtrasada = false,
                isHoje = false
            )
        )

        state = state.copy(
            isLoading = false,
            entregasAtrasadas = atrasadas,
            entregasHoje = hoje,
            entregasAgendadas = agendadas
        )

        assertFalse(state.isLoading)
        assertEquals(1, state.entregasAtrasadas.size)
        assertEquals(1, state.entregasHoje.size)
        assertEquals(1, state.entregasAgendadas.size)
    }

    @Test
    fun `Cenario - refresh de entregas`() {
        val entregasAnteriores = listOf(
            EntregaComDetalhes(
                locacao = Locacao(id = "loc-1"),
                cliente = Cliente(id = "cli-1", nomeRazao = "João"),
                equipamento = Equipamento(id = "eq-1", nome = "Betoneira"),
                isAtrasada = true,
                isHoje = false
            )
        )

        var state = EntregasContract.State(
            isLoading = false,
            entregasAtrasadas = entregasAnteriores
        )

        // Inicia refresh
        state = state.copy(isRefreshing = true)
        assertTrue(state.isRefreshing)
        // Dados antigos ainda visíveis durante refresh
        assertEquals(1, state.entregasAtrasadas.size)

        // Refresh concluído com novos dados
        state = state.copy(
            isRefreshing = false,
            entregasAtrasadas = emptyList() // Entrega foi concluída
        )

        assertFalse(state.isRefreshing)
        assertTrue(state.entregasAtrasadas.isEmpty())
    }

    @Test
    fun `Cenario - marcar entrega como realizada`() {
        val entrega = EntregaComDetalhes(
            locacao = Locacao(id = "loc-1", statusEntrega = StatusEntrega.AGENDADA),
            cliente = Cliente(id = "cli-1", nomeRazao = "João"),
            equipamento = Equipamento(id = "eq-1", nome = "Betoneira"),
            isAtrasada = false,
            isHoje = true
        )

        var state = EntregasContract.State(
            isLoading = false,
            entregasHoje = listOf(entrega)
        )

        assertEquals(1, state.entregasHoje.size)

        // Após marcar entrega, lista fica vazia
        state = state.copy(entregasHoje = emptyList())

        assertTrue(state.entregasHoje.isEmpty())
    }
}
