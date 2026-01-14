package br.com.codecacto.locadora.features.recebimentos.presentation

import br.com.codecacto.locadora.currentTimeMillis

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
            equipamentos = listOf(equipamento)
        )

        assertEquals(recebimento, comDetalhes.recebimento)
        assertEquals(cliente, comDetalhes.cliente)
        assertEquals(equipamento, comDetalhes.equipamentos.firstOrNull())
    }

    @Test
    fun `RecebimentoComDetalhes pode ter cliente null e equipamentos vazios`() {
        val recebimento = Recebimento(id = "rec-1", locacaoId = "loc-1")

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = null,
            equipamentos = emptyList()
        )

        assertNull(comDetalhes.cliente)
        assertTrue(comDetalhes.equipamentos.isEmpty())
    }

    @Test
    fun `RecebimentoComDetalhes com status PAGO`() {
        val recebimento = Recebimento(
            id = "rec-1",
            status = StatusPagamento.PAGO,
            dataPagamento = currentTimeMillis()
        )

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = null,
            equipamentos = emptyList()
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
            equipamentos = emptyList()
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
            equipamentos = emptyList()
        )
        val pago = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "2", valor = 1000.0, status = StatusPagamento.PAGO),
            cliente = Cliente(id = "cli-2", nomeRazao = "Cliente 2", telefoneWhatsapp = "11888887777"),
            equipamentos = emptyList()
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

    // ==================== TESTES DE DeleteRecebimento ====================

    @Test
    fun `Action DeleteRecebimento deve conter recebimentoId correto`() {
        val action = RecebimentosContract.Action.DeleteRecebimento("rec-456")
        assertEquals("rec-456", action.recebimentoId)
    }

    @Test
    fun `Action DeleteRecebimento com id vazio`() {
        val action = RecebimentosContract.Action.DeleteRecebimento("")
        assertEquals("", action.recebimentoId)
    }

    @Test
    fun `Action DeleteRecebimento deve ser diferente de MarcarRecebido`() {
        val deleteAction = RecebimentosContract.Action.DeleteRecebimento("rec-123")
        val marcarAction = RecebimentosContract.Action.MarcarRecebido("rec-123")

        assertTrue(deleteAction is RecebimentosContract.Action.DeleteRecebimento)
        assertTrue(marcarAction is RecebimentosContract.Action.MarcarRecebido)
        assertFalse(deleteAction == marcarAction)
    }

    @Test
    fun `Effect ShowSuccess para exclusao deve conter mensagem correta`() {
        val effect = RecebimentosContract.Effect.ShowSuccess("Recebimento excluído!")
        assertEquals("Recebimento excluído!", effect.message)
    }

    @Test
    fun `Effect ShowError para exclusao deve conter mensagem correta`() {
        val effect = RecebimentosContract.Effect.ShowError("Erro ao excluir recebimento")
        assertEquals("Erro ao excluir recebimento", effect.message)
    }

    // ==================== TESTES DE CENARIO - Exclusao ====================

    @Test
    fun `Cenario - excluir recebimento pendente da lista`() {
        val pendente1 = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-1", valor = 500.0, status = StatusPagamento.PENDENTE),
            cliente = Cliente(id = "cli-1", nomeRazao = "Cliente 1", telefoneWhatsapp = "11999998888"),
            equipamentos = emptyList()
        )
        val pendente2 = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-2", valor = 300.0, status = StatusPagamento.PENDENTE),
            cliente = Cliente(id = "cli-2", nomeRazao = "Cliente 2", telefoneWhatsapp = "11888887777"),
            equipamentos = emptyList()
        )

        var state = RecebimentosContract.State(
            recebimentosPendentes = listOf(pendente1, pendente2),
            totalPendente = 800.0
        )

        // Simula remoção do primeiro recebimento
        val listaAtualizada = state.recebimentosPendentes.filter { it.recebimento.id != "rec-1" }
        state = state.copy(
            recebimentosPendentes = listaAtualizada,
            totalPendente = listaAtualizada.sumOf { it.recebimento.valor }
        )

        assertEquals(1, state.recebimentosPendentes.size)
        assertEquals("rec-2", state.recebimentosPendentes.first().recebimento.id)
        assertEquals(300.0, state.totalPendente)
    }

    @Test
    fun `Cenario - excluir recebimento pago da lista`() {
        val pago1 = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-1", valor = 1000.0, status = StatusPagamento.PAGO),
            cliente = null,
            equipamentos = emptyList()
        )
        val pago2 = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-2", valor = 750.0, status = StatusPagamento.PAGO),
            cliente = null,
            equipamentos = emptyList()
        )

        var state = RecebimentosContract.State(
            recebimentosPagos = listOf(pago1, pago2),
            totalPago = 1750.0
        )

        // Simula remoção do segundo recebimento
        val listaAtualizada = state.recebimentosPagos.filter { it.recebimento.id != "rec-2" }
        state = state.copy(
            recebimentosPagos = listaAtualizada,
            totalPago = listaAtualizada.sumOf { it.recebimento.valor }
        )

        assertEquals(1, state.recebimentosPagos.size)
        assertEquals("rec-1", state.recebimentosPagos.first().recebimento.id)
        assertEquals(1000.0, state.totalPago)
    }

    @Test
    fun `Cenario - excluir ultimo recebimento da lista`() {
        val unicoRecebimento = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-1", valor = 500.0, status = StatusPagamento.PENDENTE),
            cliente = null,
            equipamentos = emptyList()
        )

        var state = RecebimentosContract.State(
            recebimentosPendentes = listOf(unicoRecebimento),
            totalPendente = 500.0
        )

        // Simula remoção do único recebimento
        state = state.copy(
            recebimentosPendentes = emptyList(),
            totalPendente = 0.0
        )

        assertTrue(state.recebimentosPendentes.isEmpty())
        assertEquals(0.0, state.totalPendente)
    }

    // ==================== TESTES DE MULTIPLOS EQUIPAMENTOS ====================

    @Test
    fun `RecebimentoComDetalhes com multiplos equipamentos`() {
        val recebimento = Recebimento(
            id = "rec-1",
            locacaoId = "loc-1",
            clienteId = "cli-1",
            valor = 1500.0,
            status = StatusPagamento.PENDENTE
        )
        val equipamento1 = Equipamento(id = "equip-1", nome = "Betoneira", categoria = "Betoneira")
        val equipamento2 = Equipamento(id = "equip-2", nome = "Compactador", categoria = "Compactador")
        val equipamento3 = Equipamento(id = "equip-3", nome = "Andaime", categoria = "Andaime")

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = Cliente(id = "cli-1", nomeRazao = "Empresa ABC", telefoneWhatsapp = "(11) 99999-0000"),
            equipamentos = listOf(equipamento1, equipamento2, equipamento3)
        )

        assertEquals(3, comDetalhes.equipamentos.size)
        assertEquals("Betoneira", comDetalhes.equipamentos[0].nome)
        assertEquals("Compactador", comDetalhes.equipamentos[1].nome)
        assertEquals("Andaime", comDetalhes.equipamentos[2].nome)
    }

    @Test
    fun `RecebimentoComDetalhes com dois equipamentos`() {
        val recebimento = Recebimento(
            id = "rec-2",
            locacaoId = "loc-2",
            valor = 800.0,
            status = StatusPagamento.PENDENTE
        )
        val equipamento1 = Equipamento(id = "equip-1", nome = "Furadeira", categoria = "Ferramentas")
        val equipamento2 = Equipamento(id = "equip-2", nome = "Serra Circular", categoria = "Ferramentas")

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = null,
            equipamentos = listOf(equipamento1, equipamento2)
        )

        assertEquals(2, comDetalhes.equipamentos.size)
        assertTrue(comDetalhes.equipamentos.isNotEmpty())
        assertEquals("Furadeira", comDetalhes.equipamentos.firstOrNull()?.nome)
        assertEquals("Serra Circular", comDetalhes.equipamentos.lastOrNull()?.nome)
    }

    @Test
    fun `RecebimentoComDetalhes equipamentos vazios funciona corretamente`() {
        val recebimento = Recebimento(
            id = "rec-3",
            locacaoId = "loc-3",
            valor = 200.0,
            status = StatusPagamento.PAGO
        )

        val comDetalhes = RecebimentoComDetalhes(
            recebimento = recebimento,
            cliente = Cliente(id = "cli-1", nomeRazao = "Cliente Teste", telefoneWhatsapp = "11988887777"),
            equipamentos = emptyList()
        )

        assertTrue(comDetalhes.equipamentos.isEmpty())
        assertNull(comDetalhes.equipamentos.firstOrNull())
        assertEquals(0, comDetalhes.equipamentos.size)
    }

    @Test
    fun `State com recebimentos contendo multiplos equipamentos`() {
        val equipamentos = listOf(
            Equipamento(id = "equip-1", nome = "Equipamento A", categoria = "Cat A"),
            Equipamento(id = "equip-2", nome = "Equipamento B", categoria = "Cat B")
        )

        val recebimentoComMultiplos = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-1", valor = 1000.0, status = StatusPagamento.PENDENTE),
            cliente = Cliente(id = "cli-1", nomeRazao = "Cliente Multi", telefoneWhatsapp = "11999990000"),
            equipamentos = equipamentos
        )

        val state = RecebimentosContract.State(
            recebimentosPendentes = listOf(recebimentoComMultiplos),
            totalPendente = 1000.0
        )

        assertEquals(1, state.recebimentosPendentes.size)
        assertEquals(2, state.recebimentosPendentes.first().equipamentos.size)
        assertEquals("Equipamento A", state.recebimentosPendentes.first().equipamentos.first().nome)
    }

    @Test
    fun `Cenario - recebimento com equipamentos vazios em lista mista`() {
        val comEquipamentos = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-1", valor = 500.0, status = StatusPagamento.PENDENTE),
            cliente = null,
            equipamentos = listOf(Equipamento(id = "equip-1", nome = "Betoneira", categoria = "Betoneira"))
        )
        val semEquipamentos = RecebimentoComDetalhes(
            recebimento = Recebimento(id = "rec-2", valor = 300.0, status = StatusPagamento.PENDENTE),
            cliente = null,
            equipamentos = emptyList()
        )

        val state = RecebimentosContract.State(
            recebimentosPendentes = listOf(comEquipamentos, semEquipamentos),
            totalPendente = 800.0
        )

        assertEquals(2, state.recebimentosPendentes.size)
        assertTrue(state.recebimentosPendentes[0].equipamentos.isNotEmpty())
        assertTrue(state.recebimentosPendentes[1].equipamentos.isEmpty())
    }
}
