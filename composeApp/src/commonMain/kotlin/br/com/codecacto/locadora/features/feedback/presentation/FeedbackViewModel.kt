package br.com.codecacto.locadora.features.feedback.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.FeedbackMotivo
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.data.repository.FeedbackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val feedbackRepository: FeedbackRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<FeedbackContract.State, FeedbackContract.Effect, FeedbackContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(FeedbackContract.State())
    override val state: StateFlow<FeedbackContract.State> = _state.asStateFlow()

    override fun onAction(action: FeedbackContract.Action) {
        when (action) {
            is FeedbackContract.Action.OnMotivoSelected -> updateMotivo(action.motivo)
            is FeedbackContract.Action.OnMensagemChanged -> updateMensagem(action.mensagem)
            is FeedbackContract.Action.OnSendClick -> submitFeedback()
        }
    }

    private fun updateMotivo(motivo: FeedbackMotivo) {
        _state.value = _state.value.copy(
            selectedMotivo = motivo,
            motivoError = null
        )
    }

    private fun updateMensagem(mensagem: String) {
        _state.value = _state.value.copy(
            mensagem = mensagem,
            mensagemError = null
        )
    }

    private fun submitFeedback() {
        val currentState = _state.value
        val selectedMotivo = currentState.selectedMotivo

        if (selectedMotivo == null) {
            _state.value = currentState.copy(
                motivoError = Strings.FEEDBACK_ERRO_MOTIVO
            )
            return
        }

        if (currentState.mensagem.trim().isEmpty()) {
            _state.value = currentState.copy(
                mensagemError = Strings.FEEDBACK_ERRO_MENSAGEM
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            feedbackRepository.sendFeedback(
                motivo = selectedMotivo.valor,
                mensagem = currentState.mensagem.trim()
            )
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false, isSent = true)
                    emitEffect(FeedbackContract.Effect.ShowSuccess(Strings.FEEDBACK_SUCESSO))
                    delay(1500)
                    emitEffect(FeedbackContract.Effect.NavigateBack)
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(isLoading = false)
                    emitEffect(FeedbackContract.Effect.ShowError(exception.message ?: Strings.FEEDBACK_ERRO))
                }
        }
    }
}
