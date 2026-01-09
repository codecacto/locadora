package br.com.codecacto.locadora.features.equipamentos.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Equipamento

data class EquipamentoComStatus(
    val equipamento: Equipamento,
    val isAlugado: Boolean
)

object EquipamentosContract {
    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val isSaving: Boolean = false,
        val equipamentos: List<EquipamentoComStatus> = emptyList(),
        val searchQuery: String = "",
        val showForm: Boolean = false,
        val editingEquipamento: Equipamento? = null,
        // Form fields
        val nome: String = "",
        val categoria: String = "",
        val identificacao: String = "",
        val precoDiario: String = "",
        val precoSemanal: String = "",
        val precoQuinzenal: String = "",
        val precoMensal: String = "",
        val valorCompra: String = "",
        val observacoes: String = "",
        val error: String? = null
    ) : UiState {
        val filteredEquipamentos: List<EquipamentoComStatus>
            get() = if (searchQuery.isBlank()) {
                equipamentos
            } else {
                equipamentos.filter {
                    it.equipamento.nome.contains(searchQuery, ignoreCase = true) ||
                    it.equipamento.categoria.contains(searchQuery, ignoreCase = true) ||
                    it.equipamento.identificacao?.contains(searchQuery, ignoreCase = true) == true
                }
            }
    }

    sealed class Action : UiAction {
        data class Search(val query: String) : Action()
        data object ShowForm : Action()
        data object HideForm : Action()
        data class EditEquipamento(val equipamento: Equipamento) : Action()
        data class DeleteEquipamento(val equipamento: Equipamento) : Action()
        data class SetNome(val value: String) : Action()
        data class SetCategoria(val value: String) : Action()
        data class SetIdentificacao(val value: String) : Action()
        data class SetPrecoDiario(val value: String) : Action()
        data class SetPrecoSemanal(val value: String) : Action()
        data class SetPrecoQuinzenal(val value: String) : Action()
        data class SetPrecoMensal(val value: String) : Action()
        data class SetValorCompra(val value: String) : Action()
        data class SetObservacoes(val value: String) : Action()
        data object SaveEquipamento : Action()
        data object Refresh : Action()
        data object ClearForm : Action()
    }

    sealed interface Effect : UiEffect {
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
