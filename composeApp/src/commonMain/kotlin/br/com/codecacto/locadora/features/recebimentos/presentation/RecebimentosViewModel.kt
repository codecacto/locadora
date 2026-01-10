package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RecebimentosViewModel(
    private val recebimentoRepository: RecebimentoRepository,
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
            is RecebimentosContract.Action.SelectTab -> {
                _state.value = _state.value.copy(tabSelecionada = action.tab)
            }
            is RecebimentosContract.Action.MarcarRecebido -> marcarRecebido(action.recebimentoId)
            is RecebimentosContract.Action.SelectRecebimento -> {
                emitEffect(RecebimentosContract.Effect.NavigateToDetalhes(action.recebimento.locacaoId))
            }
            is RecebimentosContract.Action.Refresh -> refreshRecebimentos()
        }
    }

    private fun refreshRecebimentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadRecebimentos(isRefresh = true)
        }
    }

    private fun loadRecebimentos(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.value = _state.value.copy(isLoading = true)
            }

            combine(
                recebimentoRepository.getRecebimentosPendentes(),
                recebimentoRepository.getRecebimentosPagos(),
                clienteRepository.getClientes(),
                equipamentoRepository.getEquipamentos()
            ) { pendentes, pagos, clientes, equipamentos ->
                val clientesMap = clientes.associateBy { it.id }
                val equipamentosMap = equipamentos.associateBy { it.id }

                val recebimentosPendentes = pendentes.map { recebimento ->
                    RecebimentoComDetalhes(
                        recebimento = recebimento,
                        cliente = clientesMap[recebimento.clienteId],
                        equipamento = equipamentosMap[recebimento.equipamentoId]
                    )
                }

                val recebimentosPagos = pagos.map { recebimento ->
                    RecebimentoComDetalhes(
                        recebimento = recebimento,
                        cliente = clientesMap[recebimento.clienteId],
                        equipamento = equipamentosMap[recebimento.equipamentoId]
                    )
                }

                Pair(recebimentosPendentes, recebimentosPagos)
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar recebimentos"
                    )
                    handleError(e)
                }
                .collect { (pendentes, pagos) ->
                    val totalPendente = pendentes.sumOf { it.recebimento.valor }
                    val totalPago = pagos.sumOf { it.recebimento.valor }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        recebimentosPendentes = pendentes,
                        recebimentosPagos = pagos,
                        totalPendente = totalPendente,
                        totalPago = totalPago,
                        error = null
                    )
                }
        }
    }

    private fun marcarRecebido(recebimentoId: String) {
        viewModelScope.launch {
            try {
                recebimentoRepository.marcarPago(recebimentoId)
                emitEffect(RecebimentosContract.Effect.ShowSuccess("Pagamento confirmado!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(RecebimentosContract.Effect.ShowError(e.message ?: "Erro ao confirmar recebimento"))
            }
        }
    }
}
