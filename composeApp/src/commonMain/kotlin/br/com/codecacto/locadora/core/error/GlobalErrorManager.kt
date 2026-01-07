package br.com.codecacto.locadora.core.error

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ErrorType {
    GENERIC,
    NO_INTERNET
}

data class ErrorState(
    val isVisible: Boolean = false,
    val message: String = "",
    val type: ErrorType = ErrorType.GENERIC
)

object GlobalErrorManager {
    private val _errorState = MutableStateFlow(ErrorState())
    val errorState: StateFlow<ErrorState> = _errorState.asStateFlow()

    fun showError(message: String) {
        _errorState.value = ErrorState(
            isVisible = true,
            message = message,
            type = ErrorType.GENERIC
        )
    }

    fun showNoInternetError() {
        _errorState.value = ErrorState(
            isVisible = true,
            message = "",
            type = ErrorType.NO_INTERNET
        )
    }

    fun hideError() {
        _errorState.value = ErrorState(
            isVisible = false,
            message = "",
            type = ErrorType.GENERIC
        )
    }

    fun clearError() {
        hideError()
    }
}
