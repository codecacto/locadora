package br.com.codecacto.locadora.features.clientes.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente

object ClientesContract {
    data class State(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val clientes: List<Cliente> = emptyList(),
        val searchQuery: String = "",
        val showForm: Boolean = false,
        val editingCliente: Cliente? = null,
        // Form fields
        val nomeRazao: String = "",
        val cpfCnpj: String = "",
        val telefoneWhatsapp: String = "",
        val email: String = "",
        val endereco: String = "",
        val precisaNotaFiscalPadrao: Boolean = false,
        val error: String? = null
    ) : UiState {
        val filteredClientes: List<Cliente>
            get() = if (searchQuery.isBlank()) {
                clientes
            } else {
                clientes.filter {
                    it.nomeRazao.contains(searchQuery, ignoreCase = true) ||
                    it.cpfCnpj?.contains(searchQuery) == true ||
                    it.telefoneWhatsapp.contains(searchQuery)
                }
            }
    }

    sealed class Action : UiAction {
        data class Search(val query: String) : Action()
        data object ShowForm : Action()
        data object HideForm : Action()
        data class EditCliente(val cliente: Cliente) : Action()
        data class DeleteCliente(val cliente: Cliente) : Action()
        data class SetNomeRazao(val value: String) : Action()
        data class SetCpfCnpj(val value: String) : Action()
        data class SetTelefoneWhatsapp(val value: String) : Action()
        data class SetEmail(val value: String) : Action()
        data class SetEndereco(val value: String) : Action()
        data class SetPrecisaNotaFiscal(val value: Boolean) : Action()
        data object SaveCliente : Action()
        data object Refresh : Action()
        data object ClearForm : Action()
    }

    sealed interface Effect : UiEffect {
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
