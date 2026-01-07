package br.com.codecacto.locadora.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginContract.State())
    val state: StateFlow<LoginContract.State> = _state.asStateFlow()

    private val _effect = Channel<LoginContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun dispatch(action: LoginContract.Action) {
        when (action) {
            is LoginContract.Action.SetEmail -> {
                _state.update { it.copy(email = action.email, error = null) }
            }

            is LoginContract.Action.SetPassword -> {
                _state.update { it.copy(password = action.password, error = null) }
            }

            LoginContract.Action.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            LoginContract.Action.Login -> login()

            LoginContract.Action.NavigateToRegister -> {
                viewModelScope.launch {
                    _effect.send(LoginContract.Effect.NavigateToRegister)
                }
            }

            LoginContract.Action.ShowForgotPasswordDialog -> {
                _state.update {
                    it.copy(
                        showForgotPasswordDialog = true,
                        forgotPasswordEmail = it.email,
                        forgotPasswordError = null
                    )
                }
            }

            LoginContract.Action.HideForgotPasswordDialog -> {
                _state.update {
                    it.copy(
                        showForgotPasswordDialog = false,
                        forgotPasswordEmail = "",
                        forgotPasswordError = null
                    )
                }
            }

            is LoginContract.Action.SetForgotPasswordEmail -> {
                _state.update { it.copy(forgotPasswordEmail = action.email) }
            }

            LoginContract.Action.SendPasswordReset -> sendPasswordReset()

            LoginContract.Action.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun login() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = Strings.AUTH_PREENCHA_EMAIL_SENHA) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.loginWithEmail(currentState.email, currentState.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LoginContract.Effect.NavigateToHome)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: Strings.AUTH_ERRO_LOGIN
                        )
                    }
                }
        }
    }

    private fun sendPasswordReset() {
        val email = _state.value.forgotPasswordEmail
        if (email.isBlank()) {
            _state.update { it.copy(forgotPasswordError = Strings.AUTH_DIGITE_EMAIL) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(forgotPasswordLoading = true, forgotPasswordError = null) }

            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            forgotPasswordLoading = false,
                            showForgotPasswordDialog = false,
                            forgotPasswordEmail = ""
                        )
                    }
                    _effect.send(LoginContract.Effect.ShowSnackbar(Strings.authEmailRecuperacaoEnviado(email)))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            forgotPasswordLoading = false,
                            forgotPasswordError = error.message ?: Strings.AUTH_ERRO_ENVIAR_EMAIL
                        )
                    }
                }
        }
    }
}
