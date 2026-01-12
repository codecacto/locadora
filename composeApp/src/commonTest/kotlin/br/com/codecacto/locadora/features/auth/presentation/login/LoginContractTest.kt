package br.com.codecacto.locadora.features.auth.presentation.login

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Testes unitários para LoginContract - State, Actions e Effects.
 */
class LoginContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = LoginContract.State()

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isPasswordVisible)
        assertNull(state.error)
        assertFalse(state.showForgotPasswordDialog)
        assertEquals("", state.forgotPasswordEmail)
        assertFalse(state.forgotPasswordLoading)
        assertNull(state.forgotPasswordError)
    }

    // ==================== TESTES DE isLoginEnabled ====================

    @Test
    fun `isLoginEnabled com campos vazios deve ser false`() {
        val state = LoginContract.State(email = "", password = "")
        assertFalse(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled com email vazio deve ser false`() {
        val state = LoginContract.State(email = "", password = "123456")
        assertFalse(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled com senha curta deve ser false`() {
        val state = LoginContract.State(email = "user@email.com", password = "12345")
        assertFalse(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled com email e senha validos deve ser true`() {
        val state = LoginContract.State(email = "user@email.com", password = "123456")
        assertTrue(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled durante loading deve ser false`() {
        val state = LoginContract.State(
            email = "user@email.com",
            password = "123456",
            isLoading = true
        )
        assertFalse(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled com senha de 6 caracteres deve ser true`() {
        val state = LoginContract.State(email = "user@email.com", password = "abcdef")
        assertTrue(state.isLoginEnabled)
    }

    @Test
    fun `isLoginEnabled com senha maior que 6 caracteres deve ser true`() {
        val state = LoginContract.State(email = "user@email.com", password = "senhasuperlonga123")
        assertTrue(state.isLoginEnabled)
    }

    // ==================== TESTES DE isEmailValid ====================

    @Test
    fun `isEmailValid com email vazio deve ser true`() {
        val state = LoginContract.State(email = "")
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email valido simples deve ser true`() {
        val state = LoginContract.State(email = "user@email.com")
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email valido com subdominio deve ser true`() {
        val state = LoginContract.State(email = "user@sub.email.com.br")
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email sem arroba deve ser false`() {
        val state = LoginContract.State(email = "useremail.com")
        assertFalse(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email sem dominio deve ser false`() {
        val state = LoginContract.State(email = "user@")
        assertFalse(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email sem TLD deve ser false`() {
        val state = LoginContract.State(email = "user@email")
        assertFalse(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email com caracteres especiais validos deve ser true`() {
        val state = LoginContract.State(email = "user+tag@email.com")
        assertTrue(state.isEmailValid)
    }

    // ==================== TESTES DE STATE - Forgot Password ====================

    @Test
    fun `State com forgot password dialog aberto`() {
        val state = LoginContract.State(
            showForgotPasswordDialog = true,
            forgotPasswordEmail = "user@email.com"
        )

        assertTrue(state.showForgotPasswordDialog)
        assertEquals("user@email.com", state.forgotPasswordEmail)
    }

    @Test
    fun `State com forgot password loading`() {
        val state = LoginContract.State(
            showForgotPasswordDialog = true,
            forgotPasswordLoading = true
        )

        assertTrue(state.forgotPasswordLoading)
    }

    @Test
    fun `State com forgot password error`() {
        val state = LoginContract.State(
            showForgotPasswordDialog = true,
            forgotPasswordError = "Email não encontrado"
        )

        assertEquals("Email não encontrado", state.forgotPasswordError)
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val initial = LoginContract.State()
        val updated = initial.copy(
            email = "user@email.com",
            password = "123456",
            isLoading = true
        )

        assertEquals("user@email.com", updated.email)
        assertEquals("123456", updated.password)
        assertTrue(updated.isLoading)
        // Campos não alterados
        assertFalse(updated.isPasswordVisible)
        assertNull(updated.error)
    }

    @Test
    fun `State com erro deve manter mensagem de erro`() {
        val state = LoginContract.State(error = "Senha incorreta")
        assertEquals("Senha incorreta", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action SetEmail deve conter email correto`() {
        val action = LoginContract.Action.SetEmail("user@email.com")
        assertEquals("user@email.com", action.email)
    }

    @Test
    fun `Action SetPassword deve conter senha correta`() {
        val action = LoginContract.Action.SetPassword("123456")
        assertEquals("123456", action.password)
    }

    @Test
    fun `Action SetForgotPasswordEmail deve conter email correto`() {
        val action = LoginContract.Action.SetForgotPasswordEmail("forgot@email.com")
        assertEquals("forgot@email.com", action.email)
    }

    @Test
    fun `Action TogglePasswordVisibility deve existir`() {
        val action = LoginContract.Action.TogglePasswordVisibility
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action Login deve existir`() {
        val action = LoginContract.Action.Login
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action NavigateToRegister deve existir`() {
        val action = LoginContract.Action.NavigateToRegister
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action ShowForgotPasswordDialog deve existir`() {
        val action = LoginContract.Action.ShowForgotPasswordDialog
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action HideForgotPasswordDialog deve existir`() {
        val action = LoginContract.Action.HideForgotPasswordDialog
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action SendPasswordReset deve existir`() {
        val action = LoginContract.Action.SendPasswordReset
        assertTrue(action is LoginContract.Action)
    }

    @Test
    fun `Action ClearError deve existir`() {
        val action = LoginContract.Action.ClearError
        assertTrue(action is LoginContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateToHome deve existir`() {
        val effect = LoginContract.Effect.NavigateToHome
        assertTrue(effect is LoginContract.Effect)
    }

    @Test
    fun `Effect NavigateToRegister deve existir`() {
        val effect = LoginContract.Effect.NavigateToRegister
        assertTrue(effect is LoginContract.Effect)
    }

    @Test
    fun `Effect ShowSnackbar deve conter mensagem correta`() {
        val effect = LoginContract.Effect.ShowSnackbar("Login realizado com sucesso!")
        assertEquals("Login realizado com sucesso!", effect.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - usuario digitando credenciais validas`() {
        var state = LoginContract.State()

        // Usuario digita email
        state = state.copy(email = "user@email.com")
        assertFalse(state.isLoginEnabled) // Ainda falta senha

        // Usuario digita senha curta
        state = state.copy(password = "12345")
        assertFalse(state.isLoginEnabled) // Senha muito curta

        // Usuario completa senha
        state = state.copy(password = "123456")
        assertTrue(state.isLoginEnabled) // Agora pode logar
    }

    @Test
    fun `Cenario - toggle password visibility`() {
        var state = LoginContract.State(isPasswordVisible = false)
        assertFalse(state.isPasswordVisible)

        state = state.copy(isPasswordVisible = true)
        assertTrue(state.isPasswordVisible)

        state = state.copy(isPasswordVisible = false)
        assertFalse(state.isPasswordVisible)
    }

    @Test
    fun `Cenario - fluxo de esqueci senha`() {
        var state = LoginContract.State()

        // Abre dialog
        state = state.copy(showForgotPasswordDialog = true)
        assertTrue(state.showForgotPasswordDialog)

        // Digita email
        state = state.copy(forgotPasswordEmail = "forgot@email.com")
        assertEquals("forgot@email.com", state.forgotPasswordEmail)

        // Inicia loading
        state = state.copy(forgotPasswordLoading = true)
        assertTrue(state.forgotPasswordLoading)

        // Sucesso - fecha dialog
        state = state.copy(
            showForgotPasswordDialog = false,
            forgotPasswordLoading = false,
            forgotPasswordEmail = ""
        )
        assertFalse(state.showForgotPasswordDialog)
    }
}
