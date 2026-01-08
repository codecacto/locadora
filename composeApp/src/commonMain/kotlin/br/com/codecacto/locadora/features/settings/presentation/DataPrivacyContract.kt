package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object DataPrivacyContract {

    data class State(
        val showDeleteDialog: Boolean = false,
        val password: String = "",
        val isDeleting: Boolean = false,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data object AccountDeleted : Effect
        data class OpenUrl(val url: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed interface Action : UiAction {
        data object ShowDeleteDialog : Action
        data object HideDeleteDialog : Action
        data class SetPassword(val password: String) : Action
        data object ConfirmDeleteAccount : Action
        data object OpenTermsOfUse : Action
        data object OpenPrivacyPolicy : Action
    }
}
