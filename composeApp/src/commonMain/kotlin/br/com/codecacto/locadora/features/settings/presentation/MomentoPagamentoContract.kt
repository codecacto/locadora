package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.MomentoPagamento

object MomentoPagamentoContract {
    data class State(
        val momentoSelecionado: MomentoPagamento = MomentoPagamento.NO_VENCIMENTO,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class SetMomento(val momento: MomentoPagamento) : Action()
        data object Save : Action()
    }
}
