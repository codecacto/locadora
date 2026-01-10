package br.com.codecacto.locadora.features.settings.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.ui.util.TipoPessoa

object DadosEmpresaContract {
    data class State(
        val nomeEmpresa: String = "",
        val telefone: String = "",
        val email: String = "",
        val endereco: String = "",
        val documento: String = "",
        val tipoPessoa: TipoPessoa = TipoPessoa.JURIDICA,
        val documentoError: String? = null,
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
        data class SetDocumento(val value: String) : Action()
        data class SetTipoPessoa(val value: TipoPessoa) : Action()
        data object Save : Action()
    }
}
