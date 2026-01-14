package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object SettingsContract {
    data class State(
        val isLoading: Boolean = false,
        val isDeletingAllData: Boolean = false,
        val currentEmail: String = ""
    ) : UiState

    sealed interface Effect : UiEffect {
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data object DeleteAllData : Action()
    }
}
