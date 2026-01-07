package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusPrazo

object DetalhesLocacaoContract {
    data class State(
        val isLoading: Boolean = true,
        val locacao: Locacao? = null,
        val cliente: Cliente? = null,
        val equipamento: Equipamento? = null,
        val statusPrazo: StatusPrazo = StatusPrazo.NORMAL,
        val showRenovarDialog: Boolean = false,
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data object MarcarPago : Action()
        data object MarcarEntregue : Action()
        data object MarcarColetado : Action()
        data object MarcarNotaEmitida : Action()
        data object ShowRenovarDialog : Action()
        data object HideRenovarDialog : Action()
        data class Renovar(val novaDataFim: Long, val novoValor: Double?) : Action()
        data object Refresh : Action()
    }

    sealed interface Effect : UiEffect {
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object NavigateBack : Effect
    }
}
