package br.com.codecacto.locadora.core.error

interface ErrorHandler {
    fun handleError(exception: Throwable)
}
