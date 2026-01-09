package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RecebimentosViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<RecebimentosContract.State, RecebimentosContract.Effect, RecebimentosContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(RecebimentosContract.State())
    override val state: StateFlow<RecebimentosContract.State> = _state.asStateFlow()

    init {
        loadRecebimentos()
    }

    override fun onAction(action: RecebimentosContract.Action) {
        when (action) {
            is RecebimentosContract.Action.MarcarRecebido -> marcarRecebido(action.locacaoId)
            is RecebimentosContract.Action.SelectLocacao -> {
                emitEffect(RecebimentosContract.Effect.NavigateToDetalhes(action.locacao.id))
            }
            is RecebimentosContract.Action.Refresh -> refreshRecebimentos()
        }
    }

    private fun refreshRecebimentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun loadRecebimentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            combine(
                locacaoRepository.getLocacoesAtivas(),
                clienteRepository.getClientes(),
                equipamentoRepository.getEquipamentos()
            ) { locacoes, clientes, equipamentos ->
                val clientesMap = clientes.associateBy { it.id }
                val equipamentosMap = equipamentos.associateBy { it.id }
                val currentTime = System.currentTimeMillis()

                // Filtrar locações pendentes de pagamento:
                // 1. Equipamento coletado E pagamento pendente
                // 2. OU período vencido E pagamento pendente
                val locacoesPendentes = locacoes.filter { locacao ->
                    locacao.statusLocacao == StatusLocacao.ATIVA &&
                    locacao.statusPagamento == StatusPagamento.PENDENTE &&
                    (locacao.statusColeta == StatusColeta.COLETADO ||
                     locacao.dataFimPrevista <= currentTime)
                }

                locacoesPendentes.map { locacao ->
                    RecebimentoComDetalhes(
                        locacao = locacao,
                        cliente = clientesMap[locacao.clienteId],
                        equipamento = equipamentosMap[locacao.equipamentoId]
                    )
                }
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar recebimentos"
                    )
                    handleError(e)
                }
                .collect { recebimentos ->
                    val totalPendente = recebimentos.sumOf { it.locacao.valorLocacao }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        recebimentosPendentes = recebimentos,
                        totalPendente = totalPendente,
                        error = null
                    )
                }
        }
    }

    private fun marcarRecebido(locacaoId: String) {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarPago(locacaoId)
                emitEffect(RecebimentosContract.Effect.ShowSuccess("Pagamento confirmado!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(RecebimentosContract.Effect.ShowError(e.message ?: "Erro ao confirmar recebimento"))
            }
        }
    }
}
