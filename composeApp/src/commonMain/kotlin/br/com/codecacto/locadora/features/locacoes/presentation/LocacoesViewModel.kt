package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusPrazo
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LocacoesViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<LocacoesContract.State, LocacoesContract.Effect, LocacoesContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(LocacoesContract.State())
    override val state: StateFlow<LocacoesContract.State> = _state.asStateFlow()

    init {
        loadLocacoes()
    }

    override fun onAction(action: LocacoesContract.Action) {
        when (action) {
            is LocacoesContract.Action.SelectTab -> {
                _state.value = _state.value.copy(tabSelecionada = action.tab)
            }
            is LocacoesContract.Action.Search -> {
                _state.value = _state.value.copy(searchQuery = action.query)
            }
            is LocacoesContract.Action.SelectLocacao -> {
                emitEffect(LocacoesContract.Effect.NavigateToDetalhes(action.locacao.id))
            }
            is LocacoesContract.Action.Refresh -> {
                refreshLocacoes()
            }
        }
    }

    private fun loadLocacoes() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                collectLocacoes()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                handleError(e)
            }
        }
    }

    private fun refreshLocacoes() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isRefreshing = true)
                // Small delay to show refresh indicator
                kotlinx.coroutines.delay(500)
                _state.value = _state.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isRefreshing = false)
                handleError(e)
            }
        }
    }

    private suspend fun collectLocacoes() {
        combine(
            locacaoRepository.getLocacoes(),
            clienteRepository.getClientes(),
            equipamentoRepository.getEquipamentos()
        ) { locacoes, clientes, equipamentos ->
            val clientesMap = clientes.associateBy { it.id }
            val equipamentosMap = equipamentos.associateBy { it.id }

            locacoes.map { locacao ->
                LocacaoComDetalhes(
                    locacao = locacao,
                    cliente = clientesMap[locacao.clienteId],
                    equipamento = equipamentosMap[locacao.equipamentoId],
                    statusPrazo = calcularStatusPrazo(locacao)
                )
            }
        }.collect { locacoesComDetalhes ->
            val ativas = locacoesComDetalhes
                .filter { it.locacao.statusLocacao == br.com.codecacto.locadora.core.model.StatusLocacao.ATIVA }
                .sortedBy { it.locacao.dataFimPrevista }

            val finalizadas = locacoesComDetalhes
                .filter { it.locacao.statusLocacao == br.com.codecacto.locadora.core.model.StatusLocacao.FINALIZADA }
                .sortedByDescending { it.locacao.criadoEm }

            _state.value = _state.value.copy(
                isLoading = false,
                locacoesAtivas = ativas,
                locacoesFinalizadas = finalizadas,
                error = null
            )
        }
    }

    private fun calcularStatusPrazo(locacao: Locacao): StatusPrazo {
        if (locacao.statusLocacao == br.com.codecacto.locadora.core.model.StatusLocacao.FINALIZADA) {
            return StatusPrazo.NORMAL
        }

        val hoje = System.currentTimeMillis()
        val dataFim = locacao.dataFimPrevista
        val diffDays = ((dataFim - hoje) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays < 0 -> StatusPrazo.VENCIDO
            diffDays <= Locacao.DIAS_ALERTA_VENCIMENTO -> StatusPrazo.PROXIMO_VENCIMENTO
            else -> StatusPrazo.NORMAL
        }
    }
}
