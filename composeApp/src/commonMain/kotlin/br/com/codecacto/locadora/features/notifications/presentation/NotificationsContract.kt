package br.com.codecacto.locadora.features.notifications.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Notificacao

object NotificationsContract {
    data class State(
        val notificacoes: List<Notificacao> = emptyList(),
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val unreadCount: Int = 0,
        val error: String? = null
    ) : UiState

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
        data class ShowSuccess(val message: String) : Effect
    }

    sealed class Action : UiAction {
        data class OnNotificacaoClick(val notificacao: Notificacao) : Action()
        data class OnMarcarComoLida(val id: String) : Action()
        data class OnExcluir(val id: String) : Action()
        data object OnMarcarTodasComoLidas : Action()
        data object OnLimparLidas : Action()
        data object Refresh : Action()
    }
}
