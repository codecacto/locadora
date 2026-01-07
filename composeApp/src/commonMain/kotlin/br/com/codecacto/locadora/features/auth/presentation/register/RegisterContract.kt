package br.com.codecacto.locadora.features.auth.presentation.register

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object RegisterContract {

    data class State(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val error: String? = null
    ) : UiState {
        val isRegisterEnabled: Boolean
            get() = name.isNotBlank() &&
                    email.isNotBlank() &&
                    isEmailValid &&
                    password.length >= 6 &&
                    password == confirmPassword &&
                    !isLoading

        val isEmailValid: Boolean
            get() = email.isBlank() || isValidEmail(email)

        val isPasswordValid: Boolean
            get() = password.isBlank() || password.length >= 6

        val doPasswordsMatch: Boolean
            get() = confirmPassword.isBlank() || password == confirmPassword

        private fun isValidEmail(email: String): Boolean {
            val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            return email.matches(emailRegex.toRegex())
        }
    }

    sealed class Action : UiAction {
        data class SetName(val name: String) : Action()
        data class SetEmail(val email: String) : Action()
        data class SetPassword(val password: String) : Action()
        data class SetConfirmPassword(val confirmPassword: String) : Action()
        data object TogglePasswordVisibility : Action()
        data object ToggleConfirmPasswordVisibility : Action()
        data object Register : Action()
        data object NavigateToLogin : Action()
        data object ClearError : Action()
    }

    sealed interface Effect : UiEffect {
        data object NavigateToHome : Effect
        data object NavigateToLogin : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
