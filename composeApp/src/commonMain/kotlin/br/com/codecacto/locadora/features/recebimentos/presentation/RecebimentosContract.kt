package br.com.codecacto.locadora.features.recebimentos.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao

data class RecebimentoComDetalhes(
    val locacao: Locacao,
    val cliente: Cliente?,
    val equipamento: Equipamento?
)

object RecebimentosContract {
    data class State(
        val isLoading: Boolean = true,
        val recebimentosPendentes: List<RecebimentoComDetalhes> = emptyList(),
        val totalPendente: Double = 0.0,
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class MarcarRecebido(val locacaoId: String) : Action()
        data class SelectLocacao(val locacao: Locacao) : Action()
        data object Refresh : Action()
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetalhes(val locacaoId: String) : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
