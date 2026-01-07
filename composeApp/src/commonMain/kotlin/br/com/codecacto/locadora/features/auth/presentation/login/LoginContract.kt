package br.com.codecacto.locadora.features.auth.presentation.login

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object LoginContract {

    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val error: String? = null,
        val showForgotPasswordDialog: Boolean = false,
        val forgotPasswordEmail: String = "",
        val forgotPasswordLoading: Boolean = false,
        val forgotPasswordError: String? = null
    ) : UiState {
        val isLoginEnabled: Boolean
            get() = email.isNotBlank() && password.length >= 6 && !isLoading

        val isEmailValid: Boolean
            get() = email.isBlank() || isValidEmail(email)

        private fun isValidEmail(email: String): Boolean {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            return email.matches(emailRegex.toRegex())
        }
    }

    sealed class Action : UiAction {
        data class SetEmail(val email: String) : Action()
        data class SetPassword(val password: String) : Action()
        data object TogglePasswordVisibility : Action()
        data object Login : Action()
        data object NavigateToRegister : Action()
        data object ShowForgotPasswordDialog : Action()
        data object HideForgotPasswordDialog : Action()
        data class SetForgotPasswordEmail(val email: String) : Action()
        data object SendPasswordReset : Action()
        data object ClearError : Action()
    }

    sealed interface Effect : UiEffect {
        data object NavigateToHome : Effect
        data object NavigateToRegister : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
