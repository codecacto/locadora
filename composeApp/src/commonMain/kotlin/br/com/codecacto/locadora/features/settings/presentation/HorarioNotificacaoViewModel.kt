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

class HorarioNotificacaoViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<HorarioNotificacaoContract.State, HorarioNotificacaoContract.Effect, HorarioNotificacaoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(HorarioNotificacaoContract.State())
    override val state: StateFlow<HorarioNotificacaoContract.State> = _state.asStateFlow()

    init {
        loadHorario()
    }

    private fun loadHorario() {
        viewModelScope.launch {
            try {
                val horario = userPreferencesRepository.getHorarioNotificacao().first()
                _state.value = _state.value.copy(
                    horario = horario,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                handleError(e)
            }
        }
    }

    override fun onAction(action: HorarioNotificacaoContract.Action) {
        when (action) {
            is HorarioNotificacaoContract.Action.SetHorario -> {
                _state.value = _state.value.copy(horario = action.horario)
            }
            is HorarioNotificacaoContract.Action.Save -> saveHorario()
        }
    }

    private fun saveHorario() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                userPreferencesRepository.setHorarioNotificacao(_state.value.horario)
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(HorarioNotificacaoContract.Effect.ShowSuccess(Strings.HORARIO_NOTIFICACAO_SUCESSO))
                emitEffect(HorarioNotificacaoContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(HorarioNotificacaoContract.Effect.ShowError(e.message ?: Strings.HORARIO_NOTIFICACAO_ERRO))
            }
        }
    }
}
