package br.com.codecacto.locadora.features.entregas.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class EntregasViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<EntregasContract.State, EntregasContract.Effect, EntregasContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(EntregasContract.State())
    override val state: StateFlow<EntregasContract.State> = _state.asStateFlow()

    init {
        loadEntregas()
    }

    override fun onAction(action: EntregasContract.Action) {
        when (action) {
            is EntregasContract.Action.MarcarEntregue -> marcarEntregue(action.locacaoId)
            is EntregasContract.Action.SelectLocacao -> {
                emitEffect(EntregasContract.Effect.NavigateToDetalhes(action.locacao.id))
            }
            is EntregasContract.Action.Refresh -> refreshEntregas()
        }
    }

    private fun refreshEntregas() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadEntregas(isRefresh = true)
        }
    }

    private fun loadEntregas(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.value = _state.value.copy(isLoading = true)
            }

            combine(
                locacaoRepository.getLocacoesAtivas(),
                clienteRepository.getClientes(),
                equipamentoRepository.getEquipamentos()
            ) { locacoes, clientes, equipamentos ->
                val clientesMap = clientes.associateBy { it.id }
                val equipamentosMap = equipamentos.associateBy { it.id }

                // Filtrar apenas locações com entrega pendente (não entregue)
                val locacoesPendentes = locacoes.filter {
                    it.statusLocacao == StatusLocacao.ATIVA &&
                    it.statusEntrega != StatusEntrega.ENTREGUE
                }

                val hoje: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                locacoesPendentes.map { locacao ->
                    // Para entrega:
                    // - NAO_AGENDADA: usar dataInicio (entrega é para o dia da locação)
                    // - AGENDADA: usar dataEntregaPrevista
                    val dataEntregaMillis = when (locacao.statusEntrega) {
                        StatusEntrega.AGENDADA -> locacao.dataEntregaPrevista
                        StatusEntrega.NAO_AGENDADA -> locacao.dataInicio
                        else -> null
                    }

                    val dataEntrega = dataEntregaMillis?.let {
                        Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }

                    val isAtrasada = dataEntrega?.let { it < hoje } ?: false
                    val isHoje = dataEntrega?.let { it == hoje } ?: false

                    EntregaComDetalhes(
                        locacao = locacao,
                        cliente = clientesMap[locacao.clienteId],
                        equipamento = equipamentosMap[locacao.equipamentoId],
                        isAtrasada = isAtrasada,
                        isHoje = isHoje
                    )
                }
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar entregas"
                    )
                    handleError(e)
                }
                .collect { entregas ->
                    val atrasadas = entregas.filter { it.isAtrasada }
                        .sortedBy { it.locacao.dataEntregaPrevista ?: it.locacao.dataInicio }
                    val hoje = entregas.filter { it.isHoje }
                    val agendadas = entregas.filter { !it.isAtrasada && !it.isHoje }
                        .sortedBy { it.locacao.dataEntregaPrevista ?: it.locacao.dataInicio }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        entregasAtrasadas = atrasadas,
                        entregasHoje = hoje,
                        entregasAgendadas = agendadas,
                        error = null
                    )
                }
        }
    }

    private fun marcarEntregue(locacaoId: String) {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarEntregue(locacaoId)
                emitEffect(EntregasContract.Effect.ShowSuccess("Equipamento marcado como entregue!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(EntregasContract.Effect.ShowError(e.message ?: "Erro ao marcar entrega"))
            }
        }
    }
}
