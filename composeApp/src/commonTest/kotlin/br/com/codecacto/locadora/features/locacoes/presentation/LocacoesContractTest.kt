package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPrazo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para LocacoesContract - State e sua lógica.
 */
class LocacoesContractTest {

    // ==================== TESTES DE LocacaoComDetalhes ====================

    @Test
    fun `LocacaoComDetalhes deve manter todos os campos`() {
        val locacao = Locacao(
            id = "loc-1",
            clienteId = "cli-1",
            equipamentoId = "equip-1",
            valorLocacao = 500.0,
            periodo = PeriodoLocacao.MENSAL
        )
        val cliente = Cliente(id = "cli-1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        val equipamento = Equipamento(id = "equip-1", nome = "Betoneira", categoria = "Betoneira")

        val comDetalhes = LocacaoComDetalhes(
            locacao = locacao,
            cliente = cliente,
            equipamento = equipamento,
            statusPrazo = StatusPrazo.NORMAL
        )

        assertEquals(locacao, comDetalhes.locacao)
        assertEquals(cliente, comDetalhes.cliente)
        assertEquals(equipamento, comDetalhes.equipamento)
        assertEquals(StatusPrazo.NORMAL, comDetalhes.statusPrazo)
    }

    @Test
    fun `LocacaoComDetalhes pode ter cliente null`() {
        val locacao = Locacao(id = "loc-1", clienteId = "cli-1", equipamentoId = "equip-1")

        val comDetalhes = LocacaoComDetalhes(
            locacao = locacao,
            cliente = null,
            equipamento = null,
            statusPrazo = StatusPrazo.NORMAL
        )

        assertNull(comDetalhes.cliente)
        assertNull(comDetalhes.equipamento)
    }

    @Test
    fun `LocacaoComDetalhes com statusPrazo VENCIDO`() {
        val locacao = Locacao(id = "loc-1", clienteId = "cli-1", equipamentoId = "equip-1")

        val comDetalhes = LocacaoComDetalhes(
            locacao = locacao,
            cliente = null,
            equipamento = null,
            statusPrazo = StatusPrazo.VENCIDO
        )

        assertEquals(StatusPrazo.VENCIDO, comDetalhes.statusPrazo)
    }

    @Test
    fun `LocacaoComDetalhes com statusPrazo PROXIMO_VENCIMENTO`() {
        val locacao = Locacao(id = "loc-1", clienteId = "cli-1", equipamentoId = "equip-1")

        val comDetalhes = LocacaoComDetalhes(
            locacao = locacao,
            cliente = null,
            equipamento = null,
            statusPrazo = StatusPrazo.PROXIMO_VENCIMENTO
        )

        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, comDetalhes.statusPrazo)
    }

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = LocacoesContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.locacoesAtivas.isEmpty())
        assertTrue(state.locacoesFinalizadas.isEmpty())
        assertEquals(0, state.tabSelecionada)
        assertEquals("", state.searchQuery)
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Tabs ====================

    @Test
    fun `State com tab 0 deve mostrar locacoes ativas`() {
        val state = LocacoesContract.State(tabSelecionada = 0)
        assertEquals(0, state.tabSelecionada)
    }

    @Test
    fun `State com tab 1 deve mostrar locacoes finalizadas`() {
        val state = LocacoesContract.State(tabSelecionada = 1)
        assertEquals(1, state.tabSelecionada)
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val locacaoAtiva = LocacaoComDetalhes(
            locacao = Locacao(id = "1", statusLocacao = StatusLocacao.ATIVA),
            cliente = null,
            equipamento = null,
            statusPrazo = StatusPrazo.NORMAL
        )

        val initial = LocacoesContract.State()
        val updated = initial.copy(
            isLoading = false,
            locacoesAtivas = listOf(locacaoAtiva),
            tabSelecionada = 1
        )

        assertFalse(updated.isLoading)
        assertEquals(1, updated.locacoesAtivas.size)
        assertEquals(1, updated.tabSelecionada)
        // Campos não alterados
        assertTrue(updated.locacoesFinalizadas.isEmpty())
    }

    @Test
    fun `State com listas de locacoes preenchidas`() {
        val ativa = LocacaoComDetalhes(
            locacao = Locacao(id = "1", statusLocacao = StatusLocacao.ATIVA),
            cliente = Cliente(id = "cli-1", nomeRazao = "Cliente 1", telefoneWhatsapp = "11999998888"),
            equipamento = null,
            statusPrazo = StatusPrazo.NORMAL
        )
        val finalizada = LocacaoComDetalhes(
            locacao = Locacao(id = "2", statusLocacao = StatusLocacao.FINALIZADA),
            cliente = Cliente(id = "cli-2", nomeRazao = "Cliente 2", telefoneWhatsapp = "11888887777"),
            equipamento = null,
            statusPrazo = StatusPrazo.NORMAL
        )

        val state = LocacoesContract.State(
            locacoesAtivas = listOf(ativa),
            locacoesFinalizadas = listOf(finalizada)
        )

        assertEquals(1, state.locacoesAtivas.size)
        assertEquals(1, state.locacoesFinalizadas.size)
        assertEquals(StatusLocacao.ATIVA, state.locacoesAtivas.first().locacao.statusLocacao)
        assertEquals(StatusLocacao.FINALIZADA, state.locacoesFinalizadas.first().locacao.statusLocacao)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action SelectTab deve conter tab correta`() {
        val action0 = LocacoesContract.Action.SelectTab(0)
        val action1 = LocacoesContract.Action.SelectTab(1)

        assertEquals(0, action0.tab)
        assertEquals(1, action1.tab)
    }

    @Test
    fun `Action Search deve conter query correta`() {
        val action = LocacoesContract.Action.Search("João")
        assertEquals("João", action.query)
    }

    @Test
    fun `Action SelectLocacao deve conter locacao correta`() {
        val locacao = Locacao(id = "loc-123", clienteId = "cli-1", equipamentoId = "equip-1")
        val action = LocacoesContract.Action.SelectLocacao(locacao)

        assertEquals(locacao, action.locacao)
    }

    @Test
    fun `Action DeleteLocacao deve conter locacao correta`() {
        val locacao = Locacao(id = "loc-123", clienteId = "cli-1", equipamentoId = "equip-1")
        val action = LocacoesContract.Action.DeleteLocacao(locacao)

        assertEquals(locacao, action.locacao)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateToDetalhes deve conter locacaoId correto`() {
        val effect = LocacoesContract.Effect.NavigateToDetalhes("loc-123")
        assertEquals("loc-123", effect.locacaoId)
    }

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = LocacoesContract.Effect.ShowSuccess("Locação excluída com sucesso!")
        assertEquals("Locação excluída com sucesso!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = LocacoesContract.Effect.ShowError("Erro ao carregar locações")
        assertEquals("Erro ao carregar locações", effect.message)
    }
}
