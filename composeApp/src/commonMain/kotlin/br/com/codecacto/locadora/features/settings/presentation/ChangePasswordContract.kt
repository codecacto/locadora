package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object ChangePasswordContract {
    data class State(
        val currentPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val showCurrentPassword: Boolean = false,
        val showNewPassword: Boolean = false,
        val showConfirmPassword: Boolean = false,
        val currentPasswordError: String? = null,
        val newPasswordError: String? = null,
        val confirmPasswordError: String? = null,
        val isLoading: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class SetCurrentPassword(val password: String) : Action()
        data class SetNewPassword(val password: String) : Action()
        data class SetConfirmPassword(val password: String) : Action()
        data object ToggleCurrentPasswordVisibility : Action()
        data object ToggleNewPasswordVisibility : Action()
        data object ToggleConfirmPasswordVisibility : Action()
        data object Submit : Action()
    }
}
