package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FaturamentoEquipamentoViewModel(
    private val equipamentoRepository: EquipamentoRepository,
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<FaturamentoEquipamentoContract.State, FaturamentoEquipamentoContract.Effect, FaturamentoEquipamentoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(FaturamentoEquipamentoContract.State())
    override val state: StateFlow<FaturamentoEquipamentoContract.State> = _state.asStateFlow()

    override fun onAction(action: FaturamentoEquipamentoContract.Action) {
        when (action) {
            is FaturamentoEquipamentoContract.Action.LoadData -> loadData(action.equipamentoId)
            is FaturamentoEquipamentoContract.Action.SelectMes -> selectMes(action.mesAno)
        }
    }

    private fun loadData(equipamentoId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val equipamento = equipamentoRepository.getEquipamentoById(equipamentoId)
                if (equipamento == null) {
                    _state.value = _state.value.copy(isLoading = false, error = "Equipamento não encontrado")
                    return@launch
                }

                val clientes = clienteRepository.getClientes().first()
                val clientesMap = clientes.associateBy { it.id }

                val todasLocacoes = locacaoRepository.getLocacoes().first()
                val locacoesPagas = todasLocacoes
                    .filter { it.equipamentoId == equipamentoId && it.statusPagamento == StatusPagamento.PAGO }
                    .map { locacao ->
                        LocacaoFaturamento(
                            locacao = locacao,
                            clienteNome = clientesMap[locacao.clienteId]?.nomeRazao ?: "Cliente não encontrado"
                        )
                    }
                    .sortedByDescending { it.locacao.dataPagamento ?: it.locacao.dataFimPrevista }

                val faturamentoTotal = locacoesPagas.sumOf { it.locacao.valorLocacao }
                val valorCompra = equipamento.valorCompra ?: 0.0
                val lucroTotal = faturamentoTotal - valorCompra

                // Extrair meses disponíveis
                val mesesDisponiveis = locacoesPagas
                    .mapNotNull { locacao ->
                        val timestamp = locacao.locacao.dataPagamento ?: locacao.locacao.dataFimPrevista
                        val instant = Instant.fromEpochMilliseconds(timestamp)
                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        MesAnoFaturamento(localDate.monthNumber, localDate.year)
                    }
                    .distinct()
                    .sortedWith(compareByDescending<MesAnoFaturamento> { it.ano }.thenByDescending { it.mes })

                _state.value = _state.value.copy(
                    isLoading = false,
                    equipamento = equipamento,
                    locacoesPagas = locacoesPagas,
                    mesesDisponiveis = mesesDisponiveis,
                    faturamentoTotal = faturamentoTotal,
                    faturamentoFiltrado = faturamentoTotal,
                    valorCompra = valorCompra,
                    lucroTotal = lucroTotal,
                    lucroFiltrado = lucroTotal,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                handleError(e)
            }
        }
    }

    private fun selectMes(mesAno: MesAnoFaturamento?) {
        val currentState = _state.value

        val locacoesFiltradas = if (mesAno == null) {
            currentState.locacoesPagas
        } else {
            currentState.locacoesPagas.filter { locacao ->
                val timestamp = locacao.locacao.dataPagamento ?: locacao.locacao.dataFimPrevista
                val instant = Instant.fromEpochMilliseconds(timestamp)
                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                localDate.monthNumber == mesAno.mes && localDate.year == mesAno.ano
            }
        }

        val faturamentoFiltrado = locacoesFiltradas.sumOf { it.locacao.valorLocacao }
        val lucroFiltrado = if (mesAno == null) {
            faturamentoFiltrado - currentState.valorCompra
        } else {
            faturamentoFiltrado // Para mês específico, mostra só o faturamento do mês
        }

        _state.value = _state.value.copy(
            mesSelecionado = mesAno,
            faturamentoFiltrado = faturamentoFiltrado,
            lucroFiltrado = if (mesAno == null) lucroFiltrado else currentState.lucroTotal
        )
    }
}
