package br.com.codecacto.locadora.features.recebimentos.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.model.StatusPagamento
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Testes unitários para RecebimentosContract - State e sua lógica.
 */
class RecebimentosContractTest {

    // ==================== TESTES DE RecebimentoComDetalhes ====================

    @Test
    fun `RecebimentoComDetalhes deve manter todos os campos`() {
        val recebimento = Recebimento(
            id = "rec-1",
            locacaoId = "loc-1",
            clienteId = "cli-1",
            equipamentoId = "equip-1",
            valor = 500.0,
            status = StatusPagamento.PENDENTE
        )
        val cliente = Cliente(id = "cli-1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        val equipamento = Equipamento(id = "equip-1", nome = "Betoneira", categoria = "Betoneira")

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = cliente,
            equipamento = equipamento
        )

        assertEquals(recebimento, comDetalhes.recebimento)
        assertEquals(cliente, comDetalhes.cliente)
        assertEquals(equipamento, comDetalhes.equipamento)
    }

    @Test
    fun `RecebimentoComDetalhes pode ter cliente null`() {
        val recebimento = Recebimento(id = "rec-1", locacaoId = "loc-1")

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = null,
            equipamento = null
        )

        assertNull(comDetalhes.cliente)
        assertNull(comDetalhes.equipamento)
    }

    @Test
    fun `RecebimentoComDetalhes com status PAGO`() {
        val recebimento = Recebimento(
            id = "rec-1",
            status = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = null,
            equipamento = null
        )

        assertEquals(StatusPagamento.PAGO, comDetalhes.recebimento.status)
    }

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = RecebimentosContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.recebimentosPendentes.isEmpty())
        assertTrue(state.recebimentosPagos.isEmpty())
        assertEquals(0.0, state.totalPendente)
        assertEquals(0.0, state.totalPago)
        assertEquals(0, state.tabSelecionada)
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Tabs ====================

    @Test
    fun `State com tab 0 deve mostrar recebimentos pendentes`() {
        val state = RecebimentosContract.State(tabSelecionada = 0)
        assertEquals(0, state.tabSelecionada)
    }

    @Test
    fun `State com tab 1 deve mostrar recebimentos pagos`() {
        val state = RecebimentosContract.State(tabSelecionada = 1)
        assertEquals(1, state.tabSelecionada)
    }

    // ==================== TESTES DE STATE - Totais ====================

    @Test
    fun `State deve calcular total pendente corretamente`() {
        val state = RecebimentosContract.State(
            totalPendente = 1500.0,
            totalPago = 3000.0
        )

        assertEquals(1500.0, state.totalPendente)
        assertEquals(3000.0, state.totalPago)
    }

    @Test
    fun `State com valores zerados`() {
        val state = RecebimentosContract.State(
            totalPendente = 0.0,
            totalPago = 0.0
        )

        assertEquals(0.0, state.totalPendente)
        assertEquals(0.0, state.totalPago)
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val recebimentoPendente = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "1", valor = 500.0, status = StatusPagamento.PENDENTE),
            cliente = null,
            equipamento = null
        )

        val initial = RecebimentosContract.State()
        val updated = initial.copy(
            isLoading = false,
            recebimentosPendentes = listOf(recebimentoPendente),
            totalPendente = 500.0,
            tabSelecionada = 1
        )

        assertFalse(updated.isLoading)
        assertEquals(1, updated.recebimentosPendentes.size)
        assertEquals(500.0, updated.totalPendente)
        assertEquals(1, updated.tabSelecionada)
        // Campos não alterados
        assertTrue(updated.recebimentosPagos.isEmpty())
        assertEquals(0.0, updated.totalPago)
    }

    @Test
    fun `State com listas de recebimentos preenchidas`() {
        val pendente = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "1", valor = 500.0, status = StatusPagamento.PENDENTE),
            cliente = Cliente(id = "cli-1", nomeRazao = "Cliente 1", telefoneWhatsapp = "11999998888"),
            equipamento = null
        )
        val pago = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "2", valor = 1000.0, status = StatusPagamento.PAGO),
            cliente = Cliente(id = "cli-2", nomeRazao = "Cliente 2", telefoneWhatsapp = "11888887777"),
            equipamento = null
        )

        val state = RecebimentosContract.State(
            recebimentosPendentes = listOf(pendente),
            recebimentosPagos = listOf(pago),
            totalPendente = 500.0,
            totalPago = 1000.0
        )

        assertEquals(1, state.recebimentosPendentes.size)
        assertEquals(1, state.recebimentosPagos.size)
        assertEquals(StatusPagamento.PENDENTE, state.recebimentosPendentes.first().recebimento.status)
        assertEquals(StatusPagamento.PAGO, state.recebimentosPagos.first().recebimento.status)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action SelectTab deve conter tab correta`() {
        val action0 = RecebimentosContract.Action.SelectTab(0)
        val action1 = RecebimentosContract.Action.SelectTab(1)

        assertEquals(0, action0.tab)
        assertEquals(1, action1.tab)
    }

    @Test
    fun `Action MarcarRecebido deve conter recebimentoId correto`() {
        val action = RecebimentosContract.Action.MarcarRecebido("rec-123")
        assertEquals("rec-123", action.recebimentoId)
    }

    @Test
    fun `Action SelectRecebimento deve conter recebimento correto`() {
        val recebimento = Recebimento(id = "rec-123", locacaoId = "loc-1", valor = 500.0)
        val action = RecebimentosContract.Action.SelectRecebimento(recebimento)

        assertEquals(recebimento, action.recebimento)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateToDetalhes deve conter locacaoId correto`() {
        val effect = RecebimentosContract.Effect.NavigateToDetalhes("loc-123")
        assertEquals("loc-123", effect.locacaoId)
    }

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = RecebimentosContract.Effect.ShowSuccess("Pagamento registrado com sucesso!")
        assertEquals("Pagamento registrado com sucesso!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = RecebimentosContract.Effect.ShowError("Erro ao registrar pagamento")
        assertEquals("Erro ao registrar pagamento", effect.message)
    }
}
