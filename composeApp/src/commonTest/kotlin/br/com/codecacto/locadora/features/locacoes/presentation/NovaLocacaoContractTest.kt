package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para NovaLocacaoContract - State, Actions e Effects.
 */
class NovaLocacaoContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = NovaLocacaoContract.State()

        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertTrue(state.clientes.isEmpty())
        assertTrue(state.equipamentosDisponiveis.isEmpty())
        assertNull(state.clienteSelecionado)
        assertNull(state.equipamentoSelecionado)
        assertTrue(state.periodosDisponiveis.isEmpty())
        assertNull(state.periodoSelecionado)
        assertEquals("", state.valorLocacao)
        assertNotNull(state.dataInicio)
        assertNull(state.dataFimPrevista)
        assertNull(state.dataVencimentoPagamento)
        assertEquals(StatusEntrega.NAO_AGENDADA, state.statusEntrega)
        assertNull(state.dataEntregaPrevista)
        assertFalse(state.emitirNota)
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Listas ====================

    @Test
    fun `State com clientes carregados`() {
        val clientes = listOf(
            Cliente(id = "cli-1", nomeRazao = "João Silva"),
            Cliente(id = "cli-2", nomeRazao = "Maria Santos")
        )
        val state = NovaLocacaoContract.State(clientes = clientes)

        assertEquals(2, state.clientes.size)
        assertEquals("João Silva", state.clientes[0].nomeRazao)
        assertEquals("Maria Santos", state.clientes[1].nomeRazao)
    }

    @Test
    fun `State com equipamentos disponiveis`() {
        val equipamentos = listOf(
            Equipamento(id = "eq-1", nome = "Betoneira 400L"),
            Equipamento(id = "eq-2", nome = "Andaime 1.5m")
        )
        val state = NovaLocacaoContract.State(equipamentosDisponiveis = equipamentos)

        assertEquals(2, state.equipamentosDisponiveis.size)
        assertEquals("Betoneira 400L", state.equipamentosDisponiveis[0].nome)
    }

    @Test
    fun `State com periodos disponiveis`() {
        val periodos = listOf(PeriodoLocacao.DIARIO, PeriodoLocacao.SEMANAL, PeriodoLocacao.MENSAL)
        val state = NovaLocacaoContract.State(periodosDisponiveis = periodos)

        assertEquals(3, state.periodosDisponiveis.size)
        assertTrue(state.periodosDisponiveis.contains(PeriodoLocacao.DIARIO))
        assertTrue(state.periodosDisponiveis.contains(PeriodoLocacao.SEMANAL))
        assertTrue(state.periodosDisponiveis.contains(PeriodoLocacao.MENSAL))
    }

    // ==================== TESTES DE STATE - Selecoes ====================

    @Test
    fun `State com cliente selecionado`() {
        val cliente = Cliente(id = "cli-1", nomeRazao = "João Silva", telefoneWhatsapp = "11999998888")
        val state = NovaLocacaoContract.State(clienteSelecionado = cliente)

        assertNotNull(state.clienteSelecionado)
        assertEquals("cli-1", state.clienteSelecionado!!.id)
        assertEquals("João Silva", state.clienteSelecionado!!.nomeRazao)
    }

    @Test
    fun `State com equipamento selecionado`() {
        val equipamento = Equipamento(
            id = "eq-1",
            nome = "Betoneira 400L",
            precoDiario = 100.0,
            precoSemanal = 500.0
        )
        val state = NovaLocacaoContract.State(equipamentoSelecionado = equipamento)

        assertNotNull(state.equipamentoSelecionado)
        assertEquals("eq-1", state.equipamentoSelecionado!!.id)
        assertEquals(100.0, state.equipamentoSelecionado!!.precoDiario)
    }

    @Test
    fun `State com periodo selecionado`() {
        val state = NovaLocacaoContract.State(periodoSelecionado = PeriodoLocacao.MENSAL)

        assertNotNull(state.periodoSelecionado)
        assertEquals(PeriodoLocacao.MENSAL, state.periodoSelecionado)
        assertEquals(30, state.periodoSelecionado!!.dias)
    }

    // ==================== TESTES DE STATE - Valores e Datas ====================

    @Test
    fun `State com valor de locacao`() {
        val state = NovaLocacaoContract.State(valorLocacao = "1500.00")

        assertEquals("1500.00", state.valorLocacao)
    }

    @Test
    fun `State com datas definidas`() {
        val now = System.currentTimeMillis()
        val futuro = now + (30 * 24 * 60 * 60 * 1000L) // 30 dias

        val state = NovaLocacaoContract.State(
            dataInicio = now,
            dataFimPrevista = futuro,
            dataVencimentoPagamento = futuro
        )

        assertEquals(now, state.dataInicio)
        assertEquals(futuro, state.dataFimPrevista)
        assertEquals(futuro, state.dataVencimentoPagamento)
    }

    // ==================== TESTES DE STATE - Status Entrega ====================

    @Test
    fun `State com status entrega NAO_AGENDADA`() {
        val state = NovaLocacaoContract.State(statusEntrega = StatusEntrega.NAO_AGENDADA)
        assertEquals(StatusEntrega.NAO_AGENDADA, state.statusEntrega)
        assertNull(state.dataEntregaPrevista)
    }

    @Test
    fun `State com status entrega AGENDADA e data prevista`() {
        val dataEntrega = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L) // 2 dias

        val state = NovaLocacaoContract.State(
            statusEntrega = StatusEntrega.AGENDADA,
            dataEntregaPrevista = dataEntrega
        )

        assertEquals(StatusEntrega.AGENDADA, state.statusEntrega)
        assertNotNull(state.dataEntregaPrevista)
        assertEquals(dataEntrega, state.dataEntregaPrevista)
    }

    // ==================== TESTES DE STATE - Loading e Saving ====================

    @Test
    fun `State durante carregamento`() {
        val state = NovaLocacaoContract.State(isLoading = true)

        assertTrue(state.isLoading)
        assertFalse(state.isSaving)
    }

    @Test
    fun `State durante salvamento`() {
        val state = NovaLocacaoContract.State(isSaving = true)

        assertFalse(state.isLoading)
        assertTrue(state.isSaving)
    }

    @Test
    fun `State com erro`() {
        val state = NovaLocacaoContract.State(error = "Erro ao carregar dados")

        assertEquals("Erro ao carregar dados", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action SelectCliente deve conter cliente correto`() {
        val cliente = Cliente(id = "cli-1", nomeRazao = "João Silva")
        val action = NovaLocacaoContract.Action.SelectCliente(cliente)

        assertEquals("cli-1", action.cliente.id)
        assertEquals("João Silva", action.cliente.nomeRazao)
    }

    @Test
    fun `Action SelectEquipamento deve conter equipamento correto`() {
        val equipamento = Equipamento(id = "eq-1", nome = "Betoneira")
        val action = NovaLocacaoContract.Action.SelectEquipamento(equipamento)

        assertEquals("eq-1", action.equipamento.id)
        assertEquals("Betoneira", action.equipamento.nome)
    }

    @Test
    fun `Action SetPeriodo deve conter periodo correto`() {
        val action = NovaLocacaoContract.Action.SetPeriodo(PeriodoLocacao.QUINZENAL)

        assertEquals(PeriodoLocacao.QUINZENAL, action.periodo)
        assertEquals(15, action.periodo.dias)
    }

    @Test
    fun `Action SetValorLocacao deve conter valor correto`() {
        val action = NovaLocacaoContract.Action.SetValorLocacao("2500.50")

        assertEquals("2500.50", action.valor)
    }

    @Test
    fun `Action SetDataInicio deve conter data correta`() {
        val data = System.currentTimeMillis()
        val action = NovaLocacaoContract.Action.SetDataInicio(data)

        assertEquals(data, action.data)
    }

    @Test
    fun `Action SetDataFimPrevista deve conter data correta`() {
        val data = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
        val action = NovaLocacaoContract.Action.SetDataFimPrevista(data)

        assertEquals(data, action.data)
    }

    @Test
    fun `Action SetDataVencimentoPagamento deve conter data correta`() {
        val data = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        val action = NovaLocacaoContract.Action.SetDataVencimentoPagamento(data)

        assertEquals(data, action.data)
    }

    @Test
    fun `Action SetStatusEntrega deve conter status correto`() {
        val action = NovaLocacaoContract.Action.SetStatusEntrega(StatusEntrega.AGENDADA)

        assertEquals(StatusEntrega.AGENDADA, action.status)
    }

    @Test
    fun `Action SetDataEntregaPrevista deve conter data correta`() {
        val data = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        val action = NovaLocacaoContract.Action.SetDataEntregaPrevista(data)

        assertEquals(data, action.data)
    }

    @Test
    fun `Action SetEmitirNota deve conter valor correto`() {
        val actionTrue = NovaLocacaoContract.Action.SetEmitirNota(true)
        val actionFalse = NovaLocacaoContract.Action.SetEmitirNota(false)

        assertTrue(actionTrue.emitir)
        assertFalse(actionFalse.emitir)
    }

    @Test
    fun `Action CriarLocacao deve existir`() {
        val action = NovaLocacaoContract.Action.CriarLocacao
        assertTrue(action is NovaLocacaoContract.Action)
    }

    @Test
    fun `Action ReloadData deve existir`() {
        val action = NovaLocacaoContract.Action.ReloadData
        assertTrue(action is NovaLocacaoContract.Action)
    }

    @Test
    fun `Action ClearForm deve existir`() {
        val action = NovaLocacaoContract.Action.ClearForm
        assertTrue(action is NovaLocacaoContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect LocacaoCriada deve existir`() {
        val effect = NovaLocacaoContract.Effect.LocacaoCriada
        assertTrue(effect is NovaLocacaoContract.Effect)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = NovaLocacaoContract.Effect.ShowError("Erro ao criar locação")

        assertEquals("Erro ao criar locação", effect.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - fluxo completo de nova locacao`() {
        var state = NovaLocacaoContract.State()

        // Carrega dados
        state = state.copy(isLoading = true)
        assertTrue(state.isLoading)

        // Dados carregados
        val clientes = listOf(Cliente(id = "cli-1", nomeRazao = "João"))
        val equipamentos = listOf(
            Equipamento(
                id = "eq-1",
                nome = "Betoneira",
                precoDiario = 100.0,
                precoSemanal = 500.0,
                precoMensal = 1500.0
            )
        )
        state = state.copy(
            isLoading = false,
            clientes = clientes,
            equipamentosDisponiveis = equipamentos
        )
        assertFalse(state.isLoading)

        // Seleciona cliente
        state = state.copy(clienteSelecionado = clientes[0])
        assertNotNull(state.clienteSelecionado)

        // Seleciona equipamento
        state = state.copy(
            equipamentoSelecionado = equipamentos[0],
            periodosDisponiveis = listOf(
                PeriodoLocacao.DIARIO,
                PeriodoLocacao.SEMANAL,
                PeriodoLocacao.MENSAL
            )
        )
        assertNotNull(state.equipamentoSelecionado)
        assertEquals(3, state.periodosDisponiveis.size)

        // Seleciona periodo
        state = state.copy(
            periodoSelecionado = PeriodoLocacao.MENSAL,
            valorLocacao = "1500.00"
        )
        assertEquals(PeriodoLocacao.MENSAL, state.periodoSelecionado)
        assertEquals("1500.00", state.valorLocacao)

        // Define datas
        val now = System.currentTimeMillis()
        state = state.copy(
            dataInicio = now,
            dataFimPrevista = now + (30 * 24 * 60 * 60 * 1000L)
        )

        // Agenda entrega
        state = state.copy(
            statusEntrega = StatusEntrega.AGENDADA,
            dataEntregaPrevista = now + (24 * 60 * 60 * 1000L)
        )
        assertEquals(StatusEntrega.AGENDADA, state.statusEntrega)

        // Inicia salvamento
        state = state.copy(isSaving = true)
        assertTrue(state.isSaving)

        // Sucesso
        state = state.copy(isSaving = false)
        assertFalse(state.isSaving)
    }

    @Test
    fun `Cenario - limpar formulario`() {
        val state = NovaLocacaoContract.State(
            clienteSelecionado = Cliente(id = "cli-1", nomeRazao = "João"),
            equipamentoSelecionado = Equipamento(id = "eq-1", nome = "Betoneira"),
            periodoSelecionado = PeriodoLocacao.MENSAL,
            valorLocacao = "1500.00",
            statusEntrega = StatusEntrega.AGENDADA
        )

        // Limpa formulario
        val stateLimpo = NovaLocacaoContract.State()

        assertNull(stateLimpo.clienteSelecionado)
        assertNull(stateLimpo.equipamentoSelecionado)
        assertNull(stateLimpo.periodoSelecionado)
        assertEquals("", stateLimpo.valorLocacao)
        assertEquals(StatusEntrega.NAO_AGENDADA, stateLimpo.statusEntrega)
    }

    @Test
    fun `Cenario - erro ao criar locacao`() {
        var state = NovaLocacaoContract.State(
            clienteSelecionado = Cliente(id = "cli-1", nomeRazao = "João"),
            equipamentoSelecionado = Equipamento(id = "eq-1", nome = "Betoneira"),
            periodoSelecionado = PeriodoLocacao.MENSAL,
            valorLocacao = "1500.00",
            isSaving = true
        )

        // Erro
        state = state.copy(
            isSaving = false,
            error = "Equipamento já está locado"
        )

        assertFalse(state.isSaving)
        assertEquals("Equipamento já está locado", state.error)
    }
}
