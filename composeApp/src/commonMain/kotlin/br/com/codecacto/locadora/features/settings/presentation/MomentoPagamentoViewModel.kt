package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MomentoPagamentoViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<MomentoPagamentoContract.State, MomentoPagamentoContract.Effect, MomentoPagamentoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(MomentoPagamentoContract.State())
    override val state: StateFlow<MomentoPagamentoContract.State> = _state.asStateFlow()

    init {
        loadMomento()
    }

    private fun loadMomento() {
        viewModelScope.launch {
            try {
                val momento = userPreferencesRepository.getMomentoPagamentoPadrao().first()
                _state.value = _state.value.copy(
                    momentoSelecionado = momento,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                handleError(e)
            }
        }
    }

    override fun onAction(action: MomentoPagamentoContract.Action) {
        when (action) {
            is MomentoPagamentoContract.Action.SetMomento -> {
                _state.value = _state.value.copy(momentoSelecionado = action.momento)
            }
            is MomentoPagamentoContract.Action.Save -> saveMomento()
        }
    }

    private fun saveMomento() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                userPreferencesRepository.setMomentoPagamentoPadrao(_state.value.momentoSelecionado)
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(MomentoPagamentoContract.Effect.ShowSuccess(Strings.MOMENTO_PAGAMENTO_SUCESSO))
                emitEffect(MomentoPagamentoContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(MomentoPagamentoContract.Effect.ShowError(e.message ?: Strings.MOMENTO_PAGAMENTO_ERRO))
            }
        }
    }
}
