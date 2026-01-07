package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NovaLocacaoViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
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
                _state.value = _state.value.copy(
                    equipamentoSelecionado = action.equipamento,
                    valorLocacao = action.equipamento.precoPadraoLocacao.toString()
                )
            }
            is NovaLocacaoContract.Action.SetValorLocacao -> {
                _state.value = _state.value.copy(valorLocacao = action.valor)
            }
            is NovaLocacaoContract.Action.SetDataInicio -> {
                _state.value = _state.value.copy(dataInicio = action.data)
            }
            is NovaLocacaoContract.Action.SetDataFimPrevista -> {
                _state.value = _state.value.copy(dataFimPrevista = action.data)
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
            is NovaLocacaoContract.Action.CriarLocacao -> {
                criarLocacao()
            }
        }
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
            emitEffect(NovaLocacaoContract.Effect.ShowError("Selecione um cliente"))
            return
        }

        if (currentState.equipamentoSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError("Selecione um equipamento"))
            return
        }

        if (currentState.dataFimPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError("Informe a data de fim prevista"))
            return
        }

        val valorLocacao = currentState.valorLocacao.toDoubleOrNull()
        if (valorLocacao == null || valorLocacao <= 0) {
            emitEffect(NovaLocacaoContract.Effect.ShowError("Informe um valor válido"))
            return
        }

        if (currentState.statusEntrega == StatusEntrega.AGENDADA && currentState.dataEntregaPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError("Informe a data de entrega prevista"))
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                val locacao = Locacao(
                    clienteId = currentState.clienteSelecionado.id,
                    equipamentoId = currentState.equipamentoSelecionado.id,
                    valorLocacao = valorLocacao,
                    dataInicio = currentState.dataInicio,
                    dataFimPrevista = currentState.dataFimPrevista,
                    statusEntrega = currentState.statusEntrega,
                    dataEntregaPrevista = if (currentState.statusEntrega == StatusEntrega.AGENDADA) {
                        currentState.dataEntregaPrevista
                    } else null,
                    emitirNota = currentState.emitirNota
                )

                locacaoRepository.addLocacao(locacao)
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(NovaLocacaoContract.Effect.LocacaoCriada)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                handleError(e)
                emitEffect(NovaLocacaoContract.Effect.ShowError(e.message ?: "Erro ao criar locação"))
            }
        }
    }
}
