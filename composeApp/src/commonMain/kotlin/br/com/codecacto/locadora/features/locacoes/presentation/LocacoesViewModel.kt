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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import br.com.codecacto.locadora.currentTimeMillis

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
            is LocacoesContract.Action.DeleteLocacao -> {
                deleteLocacao(action.locacao)
            }
            is LocacoesContract.Action.Refresh -> {
                refreshLocacoes()
            }
        }
    }

    private fun deleteLocacao(locacao: Locacao) {
        viewModelScope.launch {
            try {
                locacaoRepository.deleteLocacao(locacao.id)
                emitEffect(LocacoesContract.Effect.ShowSuccess("Locação excluída com sucesso!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(LocacoesContract.Effect.ShowError(e.message ?: "Erro ao excluir locação"))
            }
        }
    }

    private fun loadLocacoes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

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
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar locacoes"
                    )
                    handleError(e)
                }
                .collect { locacoesComDetalhes ->
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
    }

    private fun refreshLocacoes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun calcularStatusPrazo(locacao: Locacao): StatusPrazo {
        if (locacao.statusLocacao == br.com.codecacto.locadora.core.model.StatusLocacao.FINALIZADA) {
            return StatusPrazo.NORMAL
        }

        val hoje = currentTimeMillis()
        val dataFim = locacao.dataFimPrevista
        val diffDays = ((dataFim - hoje) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            diffDays < 0 -> StatusPrazo.VENCIDO
            diffDays <= Locacao.DIAS_ALERTA_VENCIMENTO -> StatusPrazo.PROXIMO_VENCIMENTO
            else -> StatusPrazo.NORMAL
        }
    }
}
