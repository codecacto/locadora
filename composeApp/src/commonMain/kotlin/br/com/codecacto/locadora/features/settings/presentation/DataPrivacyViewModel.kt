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

class DataPrivacyViewModel(
    private val authRepository: AuthRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<DataPrivacyContract.State, DataPrivacyContract.Effect, DataPrivacyContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(DataPrivacyContract.State())
    override val state: StateFlow<DataPrivacyContract.State> = _state.asStateFlow()

    companion object {
        // TODO: Substituir por URLs reais da empresa
        private const val TERMS_OF_USE_URL = "https://www.google.com"
        private const val PRIVACY_POLICY_URL = "https://www.google.com"
    }

    override fun onAction(action: DataPrivacyContract.Action) {
        when (action) {
            is DataPrivacyContract.Action.ShowDeleteDialog -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = true,
                    password = "",
                    errorMessage = null
                )
            }
            is DataPrivacyContract.Action.HideDeleteDialog -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    password = "",
                    errorMessage = null
                )
            }
            is DataPrivacyContract.Action.SetPassword -> {
                _state.value = _state.value.copy(
                    password = action.password,
                    errorMessage = null
                )
            }
            is DataPrivacyContract.Action.ConfirmDeleteAccount -> deleteAccount()
            is DataPrivacyContract.Action.OpenTermsOfUse -> {
                emitEffect(DataPrivacyContract.Effect.OpenUrl(TERMS_OF_USE_URL))
            }
            is DataPrivacyContract.Action.OpenPrivacyPolicy -> {
                emitEffect(DataPrivacyContract.Effect.OpenUrl(PRIVACY_POLICY_URL))
            }
        }
    }

    private fun deleteAccount() {
        val password = _state.value.password

        if (password.isEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = Strings.DATA_PRIVACY_DIGITE_SENHA
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isDeleting = true, errorMessage = null)

            authRepository.deleteAccount(password)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        showDeleteDialog = false
                    )
                    emitEffect(DataPrivacyContract.Effect.AccountDeleted)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: Strings.DATA_PRIVACY_ERRO_EXCLUIR
                    )
                    handleError(error)
                }
        }
    }
}
