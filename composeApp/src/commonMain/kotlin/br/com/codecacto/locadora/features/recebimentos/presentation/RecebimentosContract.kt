package br.com.codecacto.locadora.features.recebimentos.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Recebimento

data class RecebimentoComDetalhes(
    val recebimento: Recebimento,
    val cliente: Cliente?,
    val equipamento: Equipamento?
)

object RecebimentosContract {
    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val recebimentosPendentes: List<RecebimentoComDetalhes> = emptyList(),
        val recebimentosPagos: List<RecebimentoComDetalhes> = emptyList(),
        val totalPendente: Double = 0.0,
        val totalPago: Double = 0.0,
        val tabSelecionada: Int = 0, // 0 = Pendentes, 1 = Pagos
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class SelectTab(val tab: Int) : Action()
        data class MarcarRecebido(val recebimentoId: String) : Action()
        data class SelectRecebimento(val recebimento: Recebimento) : Action()
        data object Refresh : Action()
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetalhes(val locacaoId: String) : Effect
        data class ShowSuccess(val message: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
