package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangeProfileViewModel(
    private val authRepository: AuthRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<ChangeProfileContract.State, ChangeProfileContract.Effect, ChangeProfileContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(ChangeProfileContract.State())
    override val state: StateFlow<ChangeProfileContract.State> = _state.asStateFlow()

    init {
        loadCurrentName()
    }

    private fun loadCurrentName() {
        val name = authRepository.currentUser?.displayName ?: ""
        _state.value = _state.value.copy(
            currentName = name,
            newName = name
        )
    }

    override fun onAction(action: ChangeProfileContract.Action) {
        when (action) {
            is ChangeProfileContract.Action.SetName -> {
                _state.value = _state.value.copy(
                    newName = action.name,
                    nameError = validateName(action.name)
                )
            }
            is ChangeProfileContract.Action.Submit -> submitProfileChange()
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isEmpty() -> Strings.PROFILE_ERRO_NOME_VAZIO
            name.length < 2 -> Strings.PROFILE_ERRO_NOME_CURTO
            name == _state.value.currentName -> Strings.PROFILE_ERRO_NOME_IGUAL
            else -> null
        }
    }

    private fun submitProfileChange() {
        val currentState = _state.value
        val nameError = validateName(currentState.newName)

        _state.value = currentState.copy(nameError = nameError)

        if (nameError == null) {
            updateProfile()
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            authRepository.updateProfile(displayName = _state.value.newName)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentName = _state.value.newName
                    )
                    emitEffect(ChangeProfileContract.Effect.ShowSuccess(Strings.PROFILE_SUCESSO))
                    emitEffect(ChangeProfileContract.Effect.NavigateBack)
                }
                .onFailure { exception ->
                    _state.value = _state.value.copy(isLoading = false)
                    emitEffect(ChangeProfileContract.Effect.ShowError(exception.message ?: Strings.PROFILE_ERRO))
                }
        }
    }
}
