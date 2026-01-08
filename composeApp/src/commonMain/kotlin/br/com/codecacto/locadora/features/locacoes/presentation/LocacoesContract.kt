package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusPrazo

data class LocacaoComDetalhes(
    val locacao: Locacao,
    val cliente: Cliente?,
    val equipamento: Equipamento?,
    val statusPrazo: StatusPrazo
)

object LocacoesContract {
    data class State(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val locacoesAtivas: List<LocacaoComDetalhes> = emptyList(),
        val locacoesFinalizadas: List<LocacaoComDetalhes> = emptyList(),
        val tabSelecionada: Int = 0, // 0 = Ativos, 1 = Finalizados
        val searchQuery: String = "",
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class SelectTab(val tab: Int) : Action()
        data class Search(val query: String) : Action()
        data class SelectLocacao(val locacao: Locacao) : Action()
        data object Refresh : Action()
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetalhes(val locacaoId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
