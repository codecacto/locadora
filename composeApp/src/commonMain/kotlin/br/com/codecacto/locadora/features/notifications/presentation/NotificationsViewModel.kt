package br.com.codecacto.locadora.features.notifications.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.data.repository.NotificacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificacaoRepository: NotificacaoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<NotificationsContract.State, NotificationsContract.Effect, NotificationsContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(NotificationsContract.State())
    override val state: StateFlow<NotificationsContract.State> = _state.asStateFlow()

    init {
        loadNotificacoes()
    }

    private fun loadNotificacoes() {
        viewModelScope.launch {
            combine(
                notificacaoRepository.getNotificacoes(),
                notificacaoRepository.getUnreadCount()
            ) { notificacoes, unreadCount ->
                NotificationsContract.State(
                    notificacoes = notificacoes,
                    isLoading = false,
                    unreadCount = unreadCount
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    override fun onAction(action: NotificationsContract.Action) {
        when (action) {
            is NotificationsContract.Action.OnNotificacaoClick -> handleNotificacaoClick(action.notificacao)
            is NotificationsContract.Action.OnMarcarComoLida -> marcarComoLida(action.id)
            is NotificationsContract.Action.OnExcluir -> excluirNotificacao(action.id)
            is NotificationsContract.Action.OnMarcarTodasComoLidas -> marcarTodasComoLidas()
            is NotificationsContract.Action.OnLimparLidas -> limparLidas()
            is NotificationsContract.Action.Refresh -> refreshNotificacoes()
        }
    }

    private fun refreshNotificacoes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun handleNotificacaoClick(notificacao: Notificacao) {
        if (!notificacao.lida) {
            marcarComoLida(notificacao.id)
        }
    }

    private fun marcarComoLida(id: String) {
        viewModelScope.launch {
            notificacaoRepository.marcarComoLida(id)
        }
    }

    private fun marcarTodasComoLidas() {
        viewModelScope.launch {
            notificacaoRepository.marcarTodasComoLidas()
                .onSuccess {
                    emitEffect(NotificationsContract.Effect.ShowSuccess("Todas marcadas como lidas"))
                }
                .onFailure { e ->
                    emitEffect(NotificationsContract.Effect.ShowError(e.message ?: "Erro"))
                }
        }
    }

    private fun excluirNotificacao(id: String) {
        viewModelScope.launch {
            notificacaoRepository.deleteNotificacao(id)
        }
    }

    private fun limparLidas() {
        viewModelScope.launch {
            notificacaoRepository.deleteTodasLidas()
                .onSuccess {
                    emitEffect(NotificationsContract.Effect.ShowSuccess("Notificacoes lidas removidas"))
                }
                .onFailure { e ->
                    emitEffect(NotificationsContract.Effect.ShowError(e.message ?: "Erro"))
                }
        }
    }
}
