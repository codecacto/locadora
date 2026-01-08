package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState

object DadosEmpresaContract {
    data class State(
        val nomeEmpresa: String = "",
        val telefone: String = "",
        val email: String = "",
        val endereco: String = "",
        val cnpj: String = "",
        val isLoading: Boolean = false,
        val isSaving: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class SetNomeEmpresa(val value: String) : Action()
        data class SetTelefone(val value: String) : Action()
        data class SetEmail(val value: String) : Action()
        data class SetEndereco(val value: String) : Action()
        data class SetCnpj(val value: String) : Action()
        data object Save : Action()
    }
}
