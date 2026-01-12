package br.com.codecacto.locadora.core.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.BeforeTest

/**
 * Testes unitários para DefaultErrorHandler e GlobalErrorManager.
 */
class ErrorHandlerTest {

    private lateinit var errorHandler: DefaultErrorHandler

    @BeforeTest
    fun setup() {
        errorHandler = DefaultErrorHandler()
        // Limpa estado anterior
        GlobalErrorManager.hideError()
    }

    // ==================== TESTES DE ErrorType ====================

    @Test
    fun `ErrorType deve ter 2 valores`() {
        assertEquals(2, ErrorType.entries.size)
    }

    @Test
    fun `ErrorType valores devem ser GENERIC e NO_INTERNET`() {
        assertEquals(ErrorType.GENERIC, ErrorType.entries[0])
        assertEquals(ErrorType.NO_INTERNET, ErrorType.entries[1])
    }

    // ==================== TESTES DE ErrorState ====================

    @Test
    fun `ErrorState valores padrao`() {
        val state = ErrorState()

        assertFalse(state.isVisible)
        assertEquals("", state.message)
        assertEquals(ErrorType.GENERIC, state.type)
    }

    @Test
    fun `ErrorState com dados customizados`() {
        val state = ErrorState(
            isVisible = true,
            message = "Erro de teste",
            type = ErrorType.GENERIC
        )

        assertTrue(state.isVisible)
        assertEquals("Erro de teste", state.message)
        assertEquals(ErrorType.GENERIC, state.type)
    }

    @Test
    fun `ErrorState tipo NO_INTERNET`() {
        val state = ErrorState(
            isVisible = true,
            message = "",
            type = ErrorType.NO_INTERNET
        )

        assertTrue(state.isVisible)
        assertEquals(ErrorType.NO_INTERNET, state.type)
    }

    // ==================== TESTES DE GlobalErrorManager ====================

    @Test
    fun `GlobalErrorManager estado inicial`() {
        GlobalErrorManager.hideError()
        val state = GlobalErrorManager.errorState.value

        assertFalse(state.isVisible)
        assertEquals("", state.message)
        assertEquals(ErrorType.GENERIC, state.type)
    }

    @Test
    fun `GlobalErrorManager showError deve atualizar estado`() {
        GlobalErrorManager.showError("Mensagem de erro")
        val state = GlobalErrorManager.errorState.value

        assertTrue(state.isVisible)
        assertEquals("Mensagem de erro", state.message)
        assertEquals(ErrorType.GENERIC, state.type)
    }

    @Test
    fun `GlobalErrorManager showNoInternetError deve atualizar estado`() {
        GlobalErrorManager.showNoInternetError()
        val state = GlobalErrorManager.errorState.value

        assertTrue(state.isVisible)
        assertEquals("", state.message)
        assertEquals(ErrorType.NO_INTERNET, state.type)
    }

    @Test
    fun `GlobalErrorManager hideError deve limpar estado`() {
        GlobalErrorManager.showError("Erro")
        GlobalErrorManager.hideError()
        val state = GlobalErrorManager.errorState.value

        assertFalse(state.isVisible)
        assertEquals("", state.message)
    }

    @Test
    fun `GlobalErrorManager clearError deve limpar estado`() {
        GlobalErrorManager.showError("Erro")
        GlobalErrorManager.clearError()
        val state = GlobalErrorManager.errorState.value

        assertFalse(state.isVisible)
    }

    // ==================== TESTES DE DefaultErrorHandler - Traducao de Erros ====================

    @Test
    fun `handleError - senha incorreta ingles`() {
        errorHandler.handleError(Exception("The password is invalid"))
        val state = GlobalErrorManager.errorState.value

        assertTrue(state.isVisible)
        assertEquals("A senha fornecida está incorreta", state.message)
    }

    @Test
    fun `handleError - credencial incorreta`() {
        errorHandler.handleError(Exception("The supplied auth credential is incorrect"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("A senha fornecida está incorreta", state.message)
    }

    @Test
    fun `handleError - wrong-password code`() {
        errorHandler.handleError(Exception("wrong-password"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("A senha fornecida está incorreta", state.message)
    }

    @Test
    fun `handleError - email mal formatado`() {
        errorHandler.handleError(Exception("The email address is badly formatted"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("O endereço de e-mail está mal formatado", state.message)
    }

    @Test
    fun `handleError - invalid-email code`() {
        errorHandler.handleError(Exception("invalid-email"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("O endereço de e-mail está mal formatado", state.message)
    }

    @Test
    fun `handleError - usuario nao encontrado`() {
        errorHandler.handleError(Exception("There is no user record"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Usuário não encontrado", state.message)
    }

    @Test
    fun `handleError - user-not-found code`() {
        errorHandler.handleError(Exception("user-not-found"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Usuário não encontrado", state.message)
    }

    @Test
    fun `handleError - email ja em uso`() {
        errorHandler.handleError(Exception("The email address is already in use"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Este e-mail já está sendo usado", state.message)
    }

    @Test
    fun `handleError - email-already-in-use code`() {
        errorHandler.handleError(Exception("email-already-in-use"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Este e-mail já está sendo usado", state.message)
    }

    @Test
    fun `handleError - senha fraca`() {
        errorHandler.handleError(Exception("Password should be at least 6 characters"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("A senha é muito fraca. Use pelo menos 6 caracteres", state.message)
    }

    @Test
    fun `handleError - weak-password code`() {
        errorHandler.handleError(Exception("weak-password"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("A senha é muito fraca. Use pelo menos 6 caracteres", state.message)
    }

    @Test
    fun `handleError - muitas tentativas`() {
        errorHandler.handleError(Exception("Too many unsuccessful login attempts"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Muitas tentativas falhadas. Tente novamente mais tarde", state.message)
    }

    @Test
    fun `handleError - too-many-requests code`() {
        errorHandler.handleError(Exception("too-many-requests"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Muitas tentativas falhadas. Tente novamente mais tarde", state.message)
    }

    @Test
    fun `handleError - erro de rede`() {
        errorHandler.handleError(Exception("network error"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Erro de conexão. Verifique sua internet", state.message)
    }

    @Test
    fun `handleError - sem permissao`() {
        errorHandler.handleError(Exception("PERMISSION_DENIED"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Sem permissão para realizar esta ação", state.message)
    }

    @Test
    fun `handleError - nao encontrado`() {
        errorHandler.handleError(Exception("NOT_FOUND"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Registro não encontrado", state.message)
    }

    @Test
    fun `handleError - erro desconhecido deve manter mensagem original`() {
        errorHandler.handleError(Exception("Erro específico do sistema"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Erro específico do sistema", state.message)
    }

    @Test
    fun `handleError - exception sem mensagem deve mostrar erro generico`() {
        errorHandler.handleError(Exception())
        val state = GlobalErrorManager.errorState.value

        assertEquals("Erro inesperado. Tente novamente.", state.message)
    }

    // ==================== TESTES DE Case Insensitive ====================

    @Test
    fun `handleError - wrong-password deve ser case insensitive`() {
        errorHandler.handleError(Exception("WRONG-PASSWORD"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("A senha fornecida está incorreta", state.message)
    }

    @Test
    fun `handleError - network deve ser case insensitive`() {
        errorHandler.handleError(Exception("NETWORK connection failed"))
        val state = GlobalErrorManager.errorState.value

        assertEquals("Erro de conexão. Verifique sua internet", state.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - login com senha errada`() {
        GlobalErrorManager.hideError()

        errorHandler.handleError(Exception("wrong-password"))
        val state = GlobalErrorManager.errorState.value

        assertTrue(state.isVisible)
        assertEquals("A senha fornecida está incorreta", state.message)
        assertEquals(ErrorType.GENERIC, state.type)

        // Usuario ve o erro e fecha
        GlobalErrorManager.hideError()
        assertFalse(GlobalErrorManager.errorState.value.isVisible)
    }

    @Test
    fun `Cenario - registro com email duplicado`() {
        GlobalErrorManager.hideError()

        errorHandler.handleError(Exception("email-already-in-use"))
        val state = GlobalErrorManager.errorState.value

        assertTrue(state.isVisible)
        assertEquals("Este e-mail já está sendo usado", state.message)
    }

    @Test
    fun `Cenario - multiplos erros consecutivos`() {
        GlobalErrorManager.hideError()

        // Primeiro erro
        errorHandler.handleError(Exception("wrong-password"))
        assertEquals("A senha fornecida está incorreta", GlobalErrorManager.errorState.value.message)

        // Segundo erro substitui o primeiro
        errorHandler.handleError(Exception("too-many-requests"))
        assertEquals("Muitas tentativas falhadas. Tente novamente mais tarde", GlobalErrorManager.errorState.value.message)
    }

    @Test
    fun `Cenario - erro de rede seguido de erro normal`() {
        GlobalErrorManager.hideError()

        // Erro de rede
        GlobalErrorManager.showNoInternetError()
        assertEquals(ErrorType.NO_INTERNET, GlobalErrorManager.errorState.value.type)

        // Depois um erro generico
        errorHandler.handleError(Exception("wrong-password"))
        assertEquals(ErrorType.GENERIC, GlobalErrorManager.errorState.value.type)
        assertEquals("A senha fornecida está incorreta", GlobalErrorManager.errorState.value.message)
    }
}
