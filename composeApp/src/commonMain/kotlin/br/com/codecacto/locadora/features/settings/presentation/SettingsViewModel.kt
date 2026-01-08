package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val authRepository: AuthRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<SettingsContract.State, SettingsContract.Effect, SettingsContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(SettingsContract.State())
    override val state: StateFlow<SettingsContract.State> = _state.asStateFlow()

    init {
        loadCurrentEmail()
    }

    private fun loadCurrentEmail() {
        val email = authRepository.currentUser?.email ?: ""
        _state.value = _state.value.copy(currentEmail = email)
    }

    override fun onAction(action: SettingsContract.Action) {
        // No actions needed for now
    }
}
