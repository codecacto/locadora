package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.util.currencyToDouble
import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.util.calcularDiasLocacao
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import br.com.codecacto.locadora.currentTimeMillis

class NovaLocacaoViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    private val recebimentoRepository: RecebimentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<NovaLocacaoContract.State, NovaLocacaoContract.Effect, NovaLocacaoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(NovaLocacaoContract.State())
    override val state: StateFlow<NovaLocacaoContract.State> = _state.asStateFlow()

    init {
        loadData()
    }

    override fun onAction(action: NovaLocacaoContract.Action) {
        when (action) {
            is NovaLocacaoContract.Action.SelectCliente -> {
                _state.value = _state.value.copy(
                    clienteSelecionado = action.cliente,
                    emitirNota = action.cliente.precisaNotaFiscalPadrao
                )
            }
            is NovaLocacaoContract.Action.SelectEquipamento -> {
                val periodosDisponiveis = action.equipamento.getPeriodosDisponiveis()
                val primeiroPeriodo = periodosDisponiveis.firstOrNull()
                val dataFim = primeiroPeriodo?.let {
                    calcularDataFim(_state.value.dataInicio, it)
                }
                _state.value = _state.value.copy(
                    equipamentoSelecionado = action.equipamento,
                    periodosDisponiveis = periodosDisponiveis,
                    periodoSelecionado = primeiroPeriodo,
                    dataFimPrevista = dataFim,
                    dataVencimentoPagamento = dataFim
                )
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetPeriodo -> {
                val equipamento = _state.value.equipamentoSelecionado ?: return
                val dataFim = calcularDataFim(_state.value.dataInicio, action.periodo)
                _state.value = _state.value.copy(
                    periodoSelecionado = action.periodo,
                    dataFimPrevista = dataFim,
                    dataVencimentoPagamento = dataFim
                )
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetValorLocacao -> {
                _state.value = _state.value.copy(valorLocacao = action.valor)
            }
            is NovaLocacaoContract.Action.SetDataInicio -> {
                val periodo = _state.value.periodoSelecionado
                val dataFim = periodo?.let { calcularDataFim(action.data, it) }
                _state.value = _state.value.copy(
                    dataInicio = action.data,
                    dataFimPrevista = dataFim ?: _state.value.dataFimPrevista,
                    dataVencimentoPagamento = dataFim ?: _state.value.dataVencimentoPagamento
                )
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetDataFimPrevista -> {
                _state.value = _state.value.copy(dataFimPrevista = action.data)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetDataVencimentoPagamento -> {
                _state.value = _state.value.copy(dataVencimentoPagamento = action.data)
            }
            is NovaLocacaoContract.Action.SetStatusEntrega -> {
                _state.value = _state.value.copy(statusEntrega = action.status)
            }
            is NovaLocacaoContract.Action.SetDataEntregaPrevista -> {
                _state.value = _state.value.copy(dataEntregaPrevista = action.data)
            }
            is NovaLocacaoContract.Action.SetEmitirNota -> {
                _state.value = _state.value.copy(emitirNota = action.emitir)
            }
            is NovaLocacaoContract.Action.SetIncluiSabado -> {
                _state.value = _state.value.copy(incluiSabado = action.inclui)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetIncluiDomingo -> {
                _state.value = _state.value.copy(incluiDomingo = action.inclui)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.CriarLocacao -> {
                criarLocacao()
            }
            is NovaLocacaoContract.Action.ReloadData -> {
                loadData()
            }
            is NovaLocacaoContract.Action.ClearForm -> {
                clearForm()
            }
        }
    }

    private fun clearForm() {
        _state.value = _state.value.copy(
            clienteSelecionado = null,
            equipamentoSelecionado = null,
            periodosDisponiveis = emptyList(),
            periodoSelecionado = null,
            valorLocacao = "",
            dataInicio = currentTimeMillis(),
            dataFimPrevista = null,
            dataVencimentoPagamento = null,
            statusEntrega = StatusEntrega.NAO_AGENDADA,
            dataEntregaPrevista = null,
            emitirNota = false,
            incluiSabado = false,
            incluiDomingo = false,
            diasCalculados = 0,
            error = null
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                combine(
                    clienteRepository.getClientes(),
                    equipamentoRepository.getEquipamentos(),
                    locacaoRepository.getLocacoesAtivas()
                ) { clientes, equipamentos, locacoesAtivas ->
                    val equipamentosAlugadosIds = locacoesAtivas.map { it.equipamentoId }.toSet()
                    val equipamentosDisponiveis = equipamentos.filter { it.id !in equipamentosAlugadosIds }
                    Triple(clientes, equipamentosDisponiveis, equipamentos)
                }.collect { (clientes, equipamentosDisponiveis, _) ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        clientes = clientes,
                        equipamentosDisponiveis = equipamentosDisponiveis
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                handleError(e)
            }
        }
    }

    private fun criarLocacao() {
        val currentState = _state.value

        if (currentState.clienteSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_CLIENTE))
            return
        }

        if (currentState.equipamentoSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_EQUIPAMENTO))
            return
        }

        if (currentState.periodoSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_PERIODO))
            return
        }

        if (currentState.dataFimPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_DATA_FIM))
            return
        }

        val valorLocacao = currentState.valorLocacao.currencyToDouble()
        if (valorLocacao <= 0) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_VALOR))
            return
        }

        if (currentState.statusEntrega == StatusEntrega.AGENDADA && currentState.dataEntregaPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_DATA_ENTREGA))
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                val locacao = Locacao(
                    clienteId = currentState.clienteSelecionado.id,
                    equipamentoId = currentState.equipamentoSelecionado.id,
                    valorLocacao = valorLocacao,
                    periodo = currentState.periodoSelecionado,
                    dataInicio = currentState.dataInicio,
                    dataFimPrevista = currentState.dataFimPrevista,
                    incluiSabado = currentState.incluiSabado,
                    incluiDomingo = currentState.incluiDomingo,
                    statusEntrega = currentState.statusEntrega,
                    dataEntregaPrevista = if (currentState.statusEntrega == StatusEntrega.AGENDADA) {
                        currentState.dataEntregaPrevista
                    } else null,
                    emitirNota = currentState.emitirNota
                )

                val locacaoId = locacaoRepository.addLocacao(locacao)

                // Criar recebimento para esta locação
                val recebimento = Recebimento(
                    locacaoId = locacaoId,
                    clienteId = currentState.clienteSelecionado.id,
                    equipamentoId = currentState.equipamentoSelecionado.id,
                    valor = valorLocacao,
                    dataVencimento = currentState.dataVencimentoPagamento ?: currentState.dataFimPrevista,
                    numeroRenovacao = 0
                )
                recebimentoRepository.addRecebimento(recebimento)

                _state.value = _state.value.copy(isSaving = false)
                emitEffect(NovaLocacaoContract.Effect.LocacaoCriada)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                handleError(e)
                emitEffect(NovaLocacaoContract.Effect.ShowError(e.message ?: Strings.ERROR_CRIAR_LOCACAO))
            }
        }
    }

    private fun calcularDataFim(dataInicio: Long, periodo: PeriodoLocacao): Long {
        val diasEmMillis = periodo.dias * 24L * 60L * 60L * 1000L
        return dataInicio + diasEmMillis
    }

    private fun recalcularValorLocacao() {
        val currentState = _state.value
        val equipamento = currentState.equipamentoSelecionado ?: return
        val periodo = currentState.periodoSelecionado ?: return
        val dataFim = currentState.dataFimPrevista ?: return
        val precoUnitario = equipamento.getPreco(periodo) ?: return

        if (periodo == PeriodoLocacao.DIARIO) {
            // Para diária, calcula baseado no número de dias
            val dias = calcularDiasLocacao(
                dataInicioMillis = currentState.dataInicio,
                dataFimMillis = dataFim,
                incluiSabado = currentState.incluiSabado,
                incluiDomingo = currentState.incluiDomingo
            )
            val valorTotal = precoUnitario * dias
            val valorEmCentavos = (valorTotal * 100).toLong().toString()
            _state.value = _state.value.copy(
                valorLocacao = valorEmCentavos,
                diasCalculados = dias
            )
        } else {
            // Para outros períodos, usa o preço fixo do período
            val valorEmCentavos = (precoUnitario * 100).toLong().toString()
            _state.value = _state.value.copy(
                valorLocacao = valorEmCentavos,
                diasCalculados = periodo.dias
            )
        }
    }
}
