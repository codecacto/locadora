package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object ChangeEmailContract {
    data class State(
        val currentEmail: String = "",
        val newEmail: String = "",
        val password: String = "",
        val showPassword: Boolean = false,
        val newEmailError: String? = null,
        val passwordError: String? = null,
        val isLoading: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class SetNewEmail(val email: String) : Action()
        data class SetPassword(val password: String) : Action()
        data object TogglePasswordVisibility : Action()
        data object Submit : Action()
    }
}
