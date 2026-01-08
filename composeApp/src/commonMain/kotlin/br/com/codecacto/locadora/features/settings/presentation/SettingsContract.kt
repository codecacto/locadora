package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object SettingsContract {
    data class State(
        val isLoading: Boolean = false,
        val currentEmail: String = ""
    ) : UiState

    sealed interface Effect : UiEffect

    sealed class Action : UiAction
}
