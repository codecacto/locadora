package br.com.codecacto.locadora.features.feedback.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.FeedbackMotivo

object FeedbackContract {
    data class State(
        val selectedMotivo: FeedbackMotivo? = null,
        val mensagem: String = "",
        val isLoading: Boolean = false,
        val isSent: Boolean = false,
        val motivoError: String? = null,
        val mensagemError: String? = null
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class OnMotivoSelected(val motivo: FeedbackMotivo) : Action()
        data class OnMensagemChanged(val mensagem: String) : Action()
        data object OnSendClick : Action()
    }
}
