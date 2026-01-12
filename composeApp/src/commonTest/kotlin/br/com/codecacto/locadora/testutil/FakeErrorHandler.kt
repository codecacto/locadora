package br.com.codecacto.locadora.testutil

import br.com.codecacto.locadora.core.error.ErrorHandler

/**
 * Fake ErrorHandler para testes.
 * Coleta os erros tratados para verificação nos testes.
 */
class FakeErrorHandler : ErrorHandler {

    val handledErrors = mutableListOf<Throwable>()

    override fun handleError(exception: Throwable) {
        handledErrors.add(exception)
    }

    fun clear() {
        handledErrors.clear()
    }

    fun hasErrors(): Boolean = handledErrors.isNotEmpty()

    fun getLastError(): Throwable? = handledErrors.lastOrNull()
}
