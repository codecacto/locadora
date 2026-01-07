package br.com.codecacto.locadora.core.error

class DefaultErrorHandler : ErrorHandler {

    override fun handleError(exception: Throwable) {
        val errorMessage = translateFirebaseError(exception.message) ?: "Erro inesperado. Tente novamente."
        GlobalErrorManager.showError(errorMessage)
    }

    private fun translateFirebaseError(message: String?): String? {
        if (message == null) return null

        return when {
            message.contains("The supplied auth credential is incorrect", ignoreCase = true) ||
            message.contains("The password is invalid", ignoreCase = true) ||
            message.contains("wrong-password", ignoreCase = true) -> "A senha fornecida está incorreta"

            message.contains("The email address is badly formatted", ignoreCase = true) ||
            message.contains("invalid-email", ignoreCase = true) -> "O endereço de e-mail está mal formatado"

            message.contains("There is no user record", ignoreCase = true) ||
            message.contains("user-not-found", ignoreCase = true) -> "Usuário não encontrado"

            message.contains("The email address is already in use", ignoreCase = true) ||
            message.contains("email-already-in-use", ignoreCase = true) -> "Este e-mail já está sendo usado"

            message.contains("Password should be at least", ignoreCase = true) ||
            message.contains("weak-password", ignoreCase = true) -> "A senha é muito fraca. Use pelo menos 6 caracteres"

            message.contains("Too many unsuccessful login attempts", ignoreCase = true) ||
            message.contains("too-many-requests", ignoreCase = true) -> "Muitas tentativas falhadas. Tente novamente mais tarde"

            message.contains("network", ignoreCase = true) -> "Erro de conexão. Verifique sua internet"

            message.contains("PERMISSION_DENIED", ignoreCase = true) -> "Sem permissão para realizar esta ação"

            message.contains("NOT_FOUND", ignoreCase = true) -> "Registro não encontrado"

            else -> message
        }
    }
}
