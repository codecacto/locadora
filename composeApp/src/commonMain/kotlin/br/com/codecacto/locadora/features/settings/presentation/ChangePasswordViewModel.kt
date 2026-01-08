package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val authRepository: AuthRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<ChangePasswordContract.State, ChangePasswordContract.Effect, ChangePasswordContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(ChangePasswordContract.State())
    override val state: StateFlow<ChangePasswordContract.State> = _state.asStateFlow()

    override fun onAction(action: ChangePasswordContract.Action) {
        when (action) {
            is ChangePasswordContract.Action.SetCurrentPassword -> {
                _state.value = _state.value.copy(
                    currentPassword = action.password,
                    currentPasswordError = null
                )
            }
            is ChangePasswordContract.Action.SetNewPassword -> {
                _state.value = _state.value.copy(
                    newPassword = action.password,
                    newPasswordError = validatePassword(action.password)
                )
            }
            is ChangePasswordContract.Action.SetConfirmPassword -> {
                _state.value = _state.value.copy(
                    confirmPassword = action.password,
                    confirmPasswordError = if (action.password != _state.value.newPassword) {
                        "As senhas nao coincidem"
                    } else null
                )
            }
            is ChangePasswordContract.Action.ToggleCurrentPasswordVisibility -> {
                _state.value = _state.value.copy(
                    showCurrentPassword = !_state.value.showCurrentPassword
                )
            }
            is ChangePasswordContract.Action.ToggleNewPasswordVisibility -> {
                _state.value = _state.value.copy(
                    showNewPassword = !_state.value.showNewPassword
                )
            }
            is ChangePasswordContract.Action.ToggleConfirmPasswordVisibility -> {
                _state.value = _state.value.copy(
                    showConfirmPassword = !_state.value.showConfirmPassword
                )
            }
            is ChangePasswordContract.Action.Submit -> submitPasswordChange()
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.length < 6 -> "A senha deve ter pelo menos 6 caracteres"
            else -> null
        }
    }

    private fun submitPasswordChange() {
        val currentState = _state.value

        val currentPasswordError = if (currentState.currentPassword.isEmpty()) {
            "Digite sua senha atual"
        } else null

        val newPasswordError = validatePassword(currentState.newPassword)
            ?: if (currentState.newPassword.isEmpty()) "Digite a nova senha" else null

        val confirmPasswordError = when {
            currentState.confirmPassword.isEmpty() -> "Confirme a nova senha"
            currentState.confirmPassword != currentState.newPassword -> "As senhas nao coincidem"
            else -> null
        }

        _state.value = currentState.copy(
            currentPasswordError = currentPasswordError,
            newPasswordError = newPasswordError,
            confirmPasswordError = confirmPasswordError
        )

        if (currentPasswordError == null && newPasswordError == null && confirmPasswordError == null) {
            changePassword()
        }
    }

    private fun changePassword() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            authRepository.changePassword(
                currentPassword = _state.value.currentPassword,
                newPassword = _state.value.newPassword
            )
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false)
                    emitEffect(ChangePasswordContract.Effect.ShowSuccess("Senha alterada com sucesso!"))
                    emitEffect(ChangePasswordContract.Effect.NavigateBack)
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Erro ao alterar senha"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentPasswordError = errorMessage
                    )
                    emitEffect(ChangePasswordContract.Effect.ShowError(errorMessage))
                }
        }
    }
}
