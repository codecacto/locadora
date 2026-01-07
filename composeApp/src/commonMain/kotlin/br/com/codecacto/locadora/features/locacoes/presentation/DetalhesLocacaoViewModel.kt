package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPrazo
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalhesLocacaoViewModel(
    private val locacaoId: String,
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<DetalhesLocacaoContract.State, DetalhesLocacaoContract.Effect, DetalhesLocacaoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(DetalhesLocacaoContract.State())
    override val state: StateFlow<DetalhesLocacaoContract.State> = _state.asStateFlow()

    init {
        loadLocacao()
    }

    override fun onAction(action: DetalhesLocacaoContract.Action) {
        when (action) {
            is DetalhesLocacaoContract.Action.MarcarPago -> marcarPago()
            is DetalhesLocacaoContract.Action.MarcarEntregue -> marcarEntregue()
            is DetalhesLocacaoContract.Action.MarcarColetado -> marcarColetado()
            is DetalhesLocacaoContract.Action.MarcarNotaEmitida -> marcarNotaEmitida()
            is DetalhesLocacaoContract.Action.ShowRenovarDialog -> {
                _state.value = _state.value.copy(showRenovarDialog = true)
            }
            is DetalhesLocacaoContract.Action.HideRenovarDialog -> {
                _state.value = _state.value.copy(showRenovarDialog = false)
            }
            is DetalhesLocacaoContract.Action.Renovar -> {
                renovarLocacao(action.novaDataFim, action.novoValor)
            }
            is DetalhesLocacaoContract.Action.Refresh -> loadLocacao()
        }
    }

    private fun loadLocacao() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val locacao = locacaoRepository.getLocacaoById(locacaoId)
                if (locacao == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Locação não encontrada"
                    )
                    return@launch
                }

                val cliente = clienteRepository.getClienteById(locacao.clienteId)
                val equipamento = equipamentoRepository.getEquipamentoById(locacao.equipamentoId)

                _state.value = _state.value.copy(
                    isLoading = false,
                    locacao = locacao,
                    cliente = cliente,
                    equipamento = equipamento,
                    statusPrazo = calcularStatusPrazo(locacao)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                handleError(e)
            }
        }
    }

    private fun marcarPago() {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarPago(locacaoId)
                loadLocacao()
                emitEffect(DetalhesLocacaoContract.Effect.ShowSuccess("Pagamento marcado como pago!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(DetalhesLocacaoContract.Effect.ShowError(e.message ?: "Erro ao marcar pagamento"))
            }
        }
    }

    private fun marcarEntregue() {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarEntregue(locacaoId)
                loadLocacao()
                emitEffect(DetalhesLocacaoContract.Effect.ShowSuccess("Equipamento marcado como entregue!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(DetalhesLocacaoContract.Effect.ShowError(e.message ?: "Erro ao marcar entrega"))
            }
        }
    }

    private fun marcarColetado() {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarColetado(locacaoId)
                loadLocacao()
                emitEffect(DetalhesLocacaoContract.Effect.ShowSuccess("Equipamento marcado como coletado!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(DetalhesLocacaoContract.Effect.ShowError(e.message ?: "Erro ao marcar coleta"))
            }
        }
    }

    private fun marcarNotaEmitida() {
        viewModelScope.launch {
            try {
                locacaoRepository.marcarNotaEmitida(locacaoId)
                loadLocacao()
                emitEffect(DetalhesLocacaoContract.Effect.ShowSuccess("Nota fiscal marcada como emitida!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(DetalhesLocacaoContract.Effect.ShowError(e.message ?: "Erro ao marcar nota"))
            }
        }
    }

    private fun renovarLocacao(novaDataFim: Long, novoValor: Double?) {
        viewModelScope.launch {
            try {
                locacaoRepository.renovarLocacao(locacaoId, novaDataFim, novoValor)
                _state.value = _state.value.copy(showRenovarDialog = false)
                loadLocacao()
                emitEffect(DetalhesLocacaoContract.Effect.ShowSuccess("Locação renovada com sucesso!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(DetalhesLocacaoContract.Effect.ShowError(e.message ?: "Erro ao renovar locação"))
            }
        }
    }

    private fun calcularStatusPrazo(locacao: Locacao): StatusPrazo {
        if (locacao.statusLocacao == StatusLocacao.FINALIZADA) {
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
