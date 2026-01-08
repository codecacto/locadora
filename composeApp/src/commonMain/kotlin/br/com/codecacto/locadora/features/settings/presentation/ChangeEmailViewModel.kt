package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangeEmailViewModel(
    private val authRepository: AuthRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<ChangeEmailContract.State, ChangeEmailContract.Effect, ChangeEmailContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(ChangeEmailContract.State())
    override val state: StateFlow<ChangeEmailContract.State> = _state.asStateFlow()

    init {
        loadCurrentEmail()
    }

    private fun loadCurrentEmail() {
        val email = authRepository.currentUser?.email ?: ""
        _state.value = _state.value.copy(currentEmail = email)
    }

    override fun onAction(action: ChangeEmailContract.Action) {
        when (action) {
            is ChangeEmailContract.Action.SetNewEmail -> {
                _state.value = _state.value.copy(
                    newEmail = action.email,
                    newEmailError = validateEmail(action.email)
                )
            }
            is ChangeEmailContract.Action.SetPassword -> {
                _state.value = _state.value.copy(
                    password = action.password,
                    passwordError = null
                )
            }
            is ChangeEmailContract.Action.TogglePasswordVisibility -> {
                _state.value = _state.value.copy(
                    showPassword = !_state.value.showPassword
                )
            }
            is ChangeEmailContract.Action.Submit -> submitEmailChange()
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Digite o novo email"
            !email.contains("@") || !email.contains(".") -> "Email invalido"
            email == _state.value.currentEmail -> "O novo email deve ser diferente do atual"
            else -> null
        }
    }

    private fun submitEmailChange() {
        val currentState = _state.value

        val newEmailError = validateEmail(currentState.newEmail)
        val passwordError = if (currentState.password.isEmpty()) {
            "Digite sua senha para confirmar"
        } else null

        _state.value = currentState.copy(
            newEmailError = newEmailError,
            passwordError = passwordError
        )

        if (newEmailError == null && passwordError == null) {
            changeEmail()
        }
    }

    private fun changeEmail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            authRepository.changeEmail(
                newEmail = _state.value.newEmail,
                password = _state.value.password
            )
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false)
                    emitEffect(ChangeEmailContract.Effect.ShowSuccess("Email alterado com sucesso!"))
                    emitEffect(ChangeEmailContract.Effect.NavigateBack)
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        passwordError = "Senha incorreta"
                    )
                    emitEffect(ChangeEmailContract.Effect.ShowError(exception.message ?: "Erro ao alterar email"))
                }
        }
    }
}
