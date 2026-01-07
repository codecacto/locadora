package br.com.codecacto.locadora.core.base

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import br.com.codecacto.locadora.core.error.ErrorHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope

abstract class BaseViewModel<STATE : UiState, EFFECT : UiEffect, ACTION : UiAction>(
    private val errorHandler: ErrorHandler
) : ViewModel() {

    protected val viewModelScope = androidViewModelScope

    private val _uiEffect = MutableSharedFlow<EFFECT>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<EFFECT> = _uiEffect.asSharedFlow()

    abstract val state: StateFlow<STATE>

    protected abstract fun onAction(action: ACTION)

    fun dispatch(action: ACTION) {
        onAction(action)
    }

    protected fun emitEffect(effect: EFFECT) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }

    protected suspend fun emitSuspendingEffect(effect: EFFECT) {
        _uiEffect.emit(effect)
    }

    protected fun handleError(exception: Throwable) {
        errorHandler.handleError(exception)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
