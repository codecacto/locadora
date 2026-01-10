package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.MomentoPagamento
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
            is RecebimentosContract.Action.SelectTab -> {
                _state.value = _state.value.copy(tabSelecionada = action.tab)
            }
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
            loadRecebimentos(isRefresh = true)
        }
    }

    private fun loadRecebimentos(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.value = _state.value.copy(isLoading = true)
            }

            combine(
                locacaoRepository.getLocacoes(),
                clienteRepository.getClientes(),
                equipamentoRepository.getEquipamentos()
            ) { locacoes, clientes, equipamentos ->
                val clientesMap = clientes.associateBy { it.id }
                val equipamentosMap = equipamentos.associateBy { it.id }
                val currentTime = System.currentTimeMillis()

                // Filtrar locações pendentes de pagamento:
                // 1. Se momento pagamento = NO_INICIO: aparece imediatamente
                // 2. Se momento pagamento = NO_VENCIMENTO:
                //    - Equipamento coletado E pagamento pendente
                //    - OU período vencido E pagamento pendente
                val locacoesPendentes = locacoes.filter { locacao ->
                    locacao.statusLocacao == StatusLocacao.ATIVA &&
                    locacao.statusPagamento == StatusPagamento.PENDENTE &&
                    (locacao.momentoPagamento == MomentoPagamento.NO_INICIO ||
                     locacao.statusColeta == StatusColeta.COLETADO ||
                     locacao.dataFimPrevista <= currentTime)
                }

                // Filtrar locações com pagamento confirmado
                val locacoesPagas = locacoes.filter { locacao ->
                    locacao.statusPagamento == StatusPagamento.PAGO
                }.sortedByDescending { it.dataPagamento ?: it.criadoEm }

                val recebimentosPendentes = locacoesPendentes.map { locacao ->
                    RecebimentoComDetalhes(
                        locacao = locacao,
                        cliente = clientesMap[locacao.clienteId],
                        equipamento = equipamentosMap[locacao.equipamentoId]
                    )
                }

                val recebimentosPagos = locacoesPagas.map { locacao ->
                    RecebimentoComDetalhes(
                        locacao = locacao,
                        cliente = clientesMap[locacao.clienteId],
                        equipamento = equipamentosMap[locacao.equipamentoId]
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
                    val totalPendente = pendentes.sumOf { it.locacao.valorLocacao }
                    val totalPago = pagos.sumOf { it.locacao.valorLocacao }

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
