package br.com.codecacto.locadora.features.auth.presentation.register

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

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterContract.State())
    val state: StateFlow<RegisterContract.State> = _state.asStateFlow()

    private val _effect = Channel<RegisterContract.Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun dispatch(action: RegisterContract.Action) {
        when (action) {
            is RegisterContract.Action.SetName -> {
                _state.update { it.copy(name = action.name, error = null) }
            }

            is RegisterContract.Action.SetEmail -> {
                _state.update { it.copy(email = action.email, error = null) }
            }

            is RegisterContract.Action.SetPassword -> {
                _state.update { it.copy(password = action.password, error = null) }
            }

            is RegisterContract.Action.SetConfirmPassword -> {
                _state.update { it.copy(confirmPassword = action.confirmPassword, error = null) }
            }

            RegisterContract.Action.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            RegisterContract.Action.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }

            RegisterContract.Action.Register -> register()

            RegisterContract.Action.NavigateToLogin -> {
                viewModelScope.launch {
                    _effect.send(RegisterContract.Effect.NavigateToLogin)
                }
            }

            RegisterContract.Action.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun register() {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.update { it.copy(error = Strings.AUTH_DIGITE_NOME) }
            return
        }

        if (currentState.email.isBlank()) {
            _state.update { it.copy(error = Strings.AUTH_DIGITE_EMAIL) }
            return
        }

        if (!currentState.isEmailValid) {
            _state.update { it.copy(error = Strings.LOGIN_EMAIL_INVALIDO) }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(error = Strings.REGISTER_SENHA_MINIMO) }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = Strings.REGISTER_SENHAS_NAO_COINCIDEM) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            authRepository.registerWithEmail(
                email = currentState.email,
                password = currentState.password,
                displayName = currentState.name
            )
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(RegisterContract.Effect.NavigateToHome)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: Strings.AUTH_ERRO_CRIAR_CONTA
                        )
                    }
                }
        }
    }
}
