package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.core.model.StatusPrazo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para DetalhesLocacaoContract - State, Actions e Effects.
 */
class DetalhesLocacaoContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = DetalhesLocacaoContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isGeneratingReceipt)
        assertNull(state.locacao)
        assertNull(state.cliente)
        assertNull(state.equipamento)
        assertEquals(StatusPrazo.NORMAL, state.statusPrazo)
        assertFalse(state.showRenovarDialog)
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Locacao ====================

    @Test
    fun `State com locacao carregada`() {
        val locacao = Locacao(
            id = "loc-123",
            clienteId = "cli-456",
            equipamentoId = "eq-789",
            valorLocacao = 1500.0,
            periodo = PeriodoLocacao.MENSAL,
            statusPagamento = StatusPagamento.PENDENTE,
            statusEntrega = StatusEntrega.ENTREGUE,
            statusLocacao = StatusLocacao.ATIVA
        )

        val state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao
        )

        assertFalse(state.isLoading)
        assertNotNull(state.locacao)
        assertEquals("loc-123", state.locacao!!.id)
        assertEquals(1500.0, state.locacao!!.valorLocacao)
    }

    @Test
    fun `State com cliente e equipamento carregados`() {
        val cliente = Cliente(id = "cli-456", nomeRazao = "João Silva", telefoneWhatsapp = "11999998888")
        val equipamento = Equipamento(id = "eq-789", nome = "Betoneira 400L")
        val locacao = Locacao(id = "loc-123", clienteId = "cli-456", equipamentoId = "eq-789")

        val state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao,
            cliente = cliente,
            equipamento = equipamento
        )

        assertNotNull(state.cliente)
        assertNotNull(state.equipamento)
        assertEquals("João Silva", state.cliente!!.nomeRazao)
        assertEquals("Betoneira 400L", state.equipamento!!.nome)
    }

    // ==================== TESTES DE STATE - Status Prazo ====================

    @Test
    fun `State com StatusPrazo NORMAL`() {
        val state = DetalhesLocacaoContract.State(statusPrazo = StatusPrazo.NORMAL)

        assertEquals(StatusPrazo.NORMAL, state.statusPrazo)
    }

    @Test
    fun `State com StatusPrazo PROXIMO_VENCIMENTO`() {
        val state = DetalhesLocacaoContract.State(statusPrazo = StatusPrazo.PROXIMO_VENCIMENTO)

        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, state.statusPrazo)
    }

    @Test
    fun `State com StatusPrazo VENCIDO`() {
        val state = DetalhesLocacaoContract.State(statusPrazo = StatusPrazo.VENCIDO)

        assertEquals(StatusPrazo.VENCIDO, state.statusPrazo)
    }

    // ==================== TESTES DE STATE - Dialog e Loading ====================

    @Test
    fun `State com dialog de renovacao aberto`() {
        val state = DetalhesLocacaoContract.State(showRenovarDialog = true)

        assertTrue(state.showRenovarDialog)
    }

    @Test
    fun `State gerando recibo`() {
        val state = DetalhesLocacaoContract.State(isGeneratingReceipt = true)

        assertTrue(state.isGeneratingReceipt)
    }

    @Test
    fun `State com erro`() {
        val state = DetalhesLocacaoContract.State(error = "Locação não encontrada")

        assertEquals("Locação não encontrada", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action MarcarPago deve existir`() {
        val action = DetalhesLocacaoContract.Action.MarcarPago
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action MarcarEntregue deve existir`() {
        val action = DetalhesLocacaoContract.Action.MarcarEntregue
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action MarcarColetado deve existir`() {
        val action = DetalhesLocacaoContract.Action.MarcarColetado
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action MarcarNotaEmitida deve existir`() {
        val action = DetalhesLocacaoContract.Action.MarcarNotaEmitida
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action ShowRenovarDialog deve existir`() {
        val action = DetalhesLocacaoContract.Action.ShowRenovarDialog
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action HideRenovarDialog deve existir`() {
        val action = DetalhesLocacaoContract.Action.HideRenovarDialog
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action Renovar deve conter dados corretos`() {
        val novaDataFim = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
        val action = DetalhesLocacaoContract.Action.Renovar(
            novaDataFim = novaDataFim,
            novoValor = 1800.0
        )

        assertEquals(novaDataFim, action.novaDataFim)
        assertEquals(1800.0, action.novoValor)
    }

    @Test
    fun `Action Renovar com novo valor nulo`() {
        val novaDataFim = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
        val action = DetalhesLocacaoContract.Action.Renovar(
            novaDataFim = novaDataFim,
            novoValor = null
        )

        assertEquals(novaDataFim, action.novaDataFim)
        assertNull(action.novoValor)
    }

    @Test
    fun `Action Refresh deve existir`() {
        val action = DetalhesLocacaoContract.Action.Refresh
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    @Test
    fun `Action GerarRecibo deve existir`() {
        val action = DetalhesLocacaoContract.Action.GerarRecibo
        assertTrue(action is DetalhesLocacaoContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = DetalhesLocacaoContract.Effect.ShowSuccess("Pagamento registrado!")

        assertEquals("Pagamento registrado!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = DetalhesLocacaoContract.Effect.ShowError("Erro ao atualizar")

        assertEquals("Erro ao atualizar", effect.message)
    }

    @Test
    fun `Effect NavigateBack deve existir`() {
        val effect = DetalhesLocacaoContract.Effect.NavigateBack
        assertTrue(effect is DetalhesLocacaoContract.Effect)
    }

    @Test
    fun `Effect CompartilharRecibo deve conter caminho correto`() {
        val effect = DetalhesLocacaoContract.Effect.CompartilharRecibo("/path/to/recibo.pdf")

        assertEquals("/path/to/recibo.pdf", effect.filePath)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - carregar detalhes da locacao`() {
        var state = DetalhesLocacaoContract.State()

        // Inicia loading
        assertTrue(state.isLoading)

        // Dados carregados
        val locacao = Locacao(
            id = "loc-123",
            clienteId = "cli-456",
            equipamentoId = "eq-789",
            valorLocacao = 1500.0,
            statusPagamento = StatusPagamento.PENDENTE,
            statusEntrega = StatusEntrega.ENTREGUE,
            statusColeta = StatusColeta.NAO_COLETADO,
            statusLocacao = StatusLocacao.ATIVA
        )
        val cliente = Cliente(id = "cli-456", nomeRazao = "João Silva")
        val equipamento = Equipamento(id = "eq-789", nome = "Betoneira 400L")

        state = state.copy(
            isLoading = false,
            locacao = locacao,
            cliente = cliente,
            equipamento = equipamento,
            statusPrazo = StatusPrazo.NORMAL
        )

        assertFalse(state.isLoading)
        assertNotNull(state.locacao)
        assertNotNull(state.cliente)
        assertNotNull(state.equipamento)
        assertEquals(StatusPrazo.NORMAL, state.statusPrazo)
    }

    @Test
    fun `Cenario - marcar como pago`() {
        val locacaoPendente = Locacao(
            id = "loc-123",
            statusPagamento = StatusPagamento.PENDENTE
        )

        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacaoPendente
        )

        assertEquals(StatusPagamento.PENDENTE, state.locacao!!.statusPagamento)

        // Atualiza para pago
        val locacaoPaga = locacaoPendente.copy(
            statusPagamento = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )
        state = state.copy(locacao = locacaoPaga)

        assertEquals(StatusPagamento.PAGO, state.locacao!!.statusPagamento)
        assertNotNull(state.locacao!!.dataPagamento)
    }

    @Test
    fun `Cenario - marcar como entregue`() {
        val locacao = Locacao(
            id = "loc-123",
            statusEntrega = StatusEntrega.AGENDADA
        )

        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao
        )

        assertEquals(StatusEntrega.AGENDADA, state.locacao!!.statusEntrega)

        // Atualiza para entregue
        val locacaoEntregue = locacao.copy(
            statusEntrega = StatusEntrega.ENTREGUE,
            dataEntregaReal = System.currentTimeMillis()
        )
        state = state.copy(locacao = locacaoEntregue)

        assertEquals(StatusEntrega.ENTREGUE, state.locacao!!.statusEntrega)
        assertNotNull(state.locacao!!.dataEntregaReal)
    }

    @Test
    fun `Cenario - marcar como coletado`() {
        val locacao = Locacao(
            id = "loc-123",
            statusColeta = StatusColeta.NAO_COLETADO
        )

        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao
        )

        assertEquals(StatusColeta.NAO_COLETADO, state.locacao!!.statusColeta)

        // Atualiza para coletado
        val locacaoColetada = locacao.copy(
            statusColeta = StatusColeta.COLETADO,
            dataColeta = System.currentTimeMillis()
        )
        state = state.copy(locacao = locacaoColetada)

        assertEquals(StatusColeta.COLETADO, state.locacao!!.statusColeta)
        assertNotNull(state.locacao!!.dataColeta)
    }

    @Test
    fun `Cenario - fluxo de renovacao`() {
        val locacao = Locacao(
            id = "loc-123",
            valorLocacao = 1500.0,
            qtdRenovacoes = 0
        )

        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao
        )

        // Abre dialog
        state = state.copy(showRenovarDialog = true)
        assertTrue(state.showRenovarDialog)

        // Fecha dialog e renova
        val locacaoRenovada = locacao.copy(
            qtdRenovacoes = 1,
            valorLocacao = 1600.0,
            ultimaRenovacaoEm = System.currentTimeMillis()
        )
        state = state.copy(
            showRenovarDialog = false,
            locacao = locacaoRenovada
        )

        assertFalse(state.showRenovarDialog)
        assertEquals(1, state.locacao!!.qtdRenovacoes)
        assertEquals(1600.0, state.locacao!!.valorLocacao)
        assertNotNull(state.locacao!!.ultimaRenovacaoEm)
    }

    @Test
    fun `Cenario - gerar recibo PDF`() {
        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = Locacao(id = "loc-123")
        )

        // Inicia geracao
        state = state.copy(isGeneratingReceipt = true)
        assertTrue(state.isGeneratingReceipt)

        // Concluido
        state = state.copy(isGeneratingReceipt = false)
        assertFalse(state.isGeneratingReceipt)
    }

    @Test
    fun `Cenario - locacao vencida deve mostrar StatusPrazo VENCIDO`() {
        val state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = Locacao(
                id = "loc-123",
                dataFimPrevista = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // Ontem
            ),
            statusPrazo = StatusPrazo.VENCIDO
        )

        assertEquals(StatusPrazo.VENCIDO, state.statusPrazo)
    }

    @Test
    fun `Cenario - locacao proximo do vencimento`() {
        val state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = Locacao(
                id = "loc-123",
                dataFimPrevista = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // Amanha
            ),
            statusPrazo = StatusPrazo.PROXIMO_VENCIMENTO
        )

        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, state.statusPrazo)
    }

    @Test
    fun `Cenario - finalizar locacao`() {
        val locacao = Locacao(
            id = "loc-123",
            statusPagamento = StatusPagamento.PAGO,
            statusColeta = StatusColeta.COLETADO,
            statusLocacao = StatusLocacao.ATIVA
        )

        var state = DetalhesLocacaoContract.State(
            isLoading = false,
            locacao = locacao
        )

        // Finaliza
        val locacaoFinalizada = locacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        state = state.copy(locacao = locacaoFinalizada)

        assertEquals(StatusLocacao.FINALIZADA, state.locacao!!.statusLocacao)
    }
}
