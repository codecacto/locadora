package br.com.codecacto.locadora.features.entregas.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao

data class EntregaComDetalhes(
    val locacao: Locacao,
    val cliente: Cliente?,
    val equipamento: Equipamento?,
    val isAtrasada: Boolean,
    val isHoje: Boolean
)

object EntregasContract {
    data class State(
        val isLoading: Boolean = true,
        val entregasAtrasadas: List<EntregaComDetalhes> = emptyList(),
        val entregasHoje: List<EntregaComDetalhes> = emptyList(),
        val entregasAgendadas: List<EntregaComDetalhes> = emptyList(),
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class MarcarEntregue(val locacaoId: String) : Action()
        data class SelectLocacao(val locacao: Locacao) : Action()
        data object Refresh : Action()
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetalhes(val locacaoId: String) : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
