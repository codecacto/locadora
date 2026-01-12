package br.com.codecacto.locadora.features.auth.presentation.register

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Testes unitários para RegisterContract - State, Actions e Effects.
 */
class RegisterContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = RegisterContract.State()

        assertEquals("", state.name)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isConfirmPasswordVisible)
        assertNull(state.error)
    }

    // ==================== TESTES DE isRegisterEnabled ====================

    @Test
    fun `isRegisterEnabled com campos vazios deve ser false`() {
        val state = RegisterContract.State()
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com nome vazio deve ser false`() {
        val state = RegisterContract.State(
            name = "",
            email = "user@email.com",
            password = "123456",
            confirmPassword = "123456"
        )
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com email vazio deve ser false`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "",
            password = "123456",
            confirmPassword = "123456"
        )
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com email invalido deve ser false`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "emailinvalido",
            password = "123456",
            confirmPassword = "123456"
        )
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com senha curta deve ser false`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "user@email.com",
            password = "12345",
            confirmPassword = "12345"
        )
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com senhas diferentes deve ser false`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "user@email.com",
            password = "123456",
            confirmPassword = "654321"
        )
        assertFalse(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled com todos campos validos deve ser true`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "user@email.com",
            password = "123456",
            confirmPassword = "123456"
        )
        assertTrue(state.isRegisterEnabled)
    }

    @Test
    fun `isRegisterEnabled durante loading deve ser false`() {
        val state = RegisterContract.State(
            name = "Usuario",
            email = "user@email.com",
            password = "123456",
            confirmPassword = "123456",
            isLoading = true
        )
        assertFalse(state.isRegisterEnabled)
    }

    // ==================== TESTES DE isEmailValid ====================

    @Test
    fun `isEmailValid com email vazio deve ser true`() {
        val state = RegisterContract.State(email = "")
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email valido deve ser true`() {
        val state = RegisterContract.State(email = "user@email.com")
        assertTrue(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email invalido deve ser false`() {
        val state = RegisterContract.State(email = "emailinvalido")
        assertFalse(state.isEmailValid)
    }

    @Test
    fun `isEmailValid com email sem TLD deve ser false`() {
        val state = RegisterContract.State(email = "user@email")
        assertFalse(state.isEmailValid)
    }

    // ==================== TESTES DE isPasswordValid ====================

    @Test
    fun `isPasswordValid com senha vazia deve ser true`() {
        val state = RegisterContract.State(password = "")
        assertTrue(state.isPasswordValid)
    }

    @Test
    fun `isPasswordValid com senha de 6 caracteres deve ser true`() {
        val state = RegisterContract.State(password = "123456")
        assertTrue(state.isPasswordValid)
    }

    @Test
    fun `isPasswordValid com senha maior que 6 caracteres deve ser true`() {
        val state = RegisterContract.State(password = "senhasuperlonga")
        assertTrue(state.isPasswordValid)
    }

    @Test
    fun `isPasswordValid com senha menor que 6 caracteres deve ser false`() {
        val state = RegisterContract.State(password = "12345")
        assertFalse(state.isPasswordValid)
    }

    // ==================== TESTES DE doPasswordsMatch ====================

    @Test
    fun `doPasswordsMatch com confirmacao vazia deve ser true`() {
        val state = RegisterContract.State(
            password = "123456",
            confirmPassword = ""
        )
        assertTrue(state.doPasswordsMatch)
    }

    @Test
    fun `doPasswordsMatch com senhas iguais deve ser true`() {
        val state = RegisterContract.State(
            password = "123456",
            confirmPassword = "123456"
        )
        assertTrue(state.doPasswordsMatch)
    }

    @Test
    fun `doPasswordsMatch com senhas diferentes deve ser false`() {
        val state = RegisterContract.State(
            password = "123456",
            confirmPassword = "654321"
        )
        assertFalse(state.doPasswordsMatch)
    }

    @Test
    fun `doPasswordsMatch com senhas parcialmente iguais deve ser false`() {
        val state = RegisterContract.State(
            password = "123456",
            confirmPassword = "12345"
        )
        assertFalse(state.doPasswordsMatch)
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val initial = RegisterContract.State()
        val updated = initial.copy(
            name = "Usuario",
            email = "user@email.com",
            password = "123456"
        )

        assertEquals("Usuario", updated.name)
        assertEquals("user@email.com", updated.email)
        assertEquals("123456", updated.password)
        // Campos não alterados
        assertEquals("", updated.confirmPassword)
        assertFalse(updated.isLoading)
    }

    @Test
    fun `State com erro deve manter mensagem`() {
        val state = RegisterContract.State(error = "Email já cadastrado")
        assertEquals("Email já cadastrado", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action SetName deve conter nome correto`() {
        val action = RegisterContract.Action.SetName("Usuario Teste")
        assertEquals("Usuario Teste", action.name)
    }

    @Test
    fun `Action SetEmail deve conter email correto`() {
        val action = RegisterContract.Action.SetEmail("user@email.com")
        assertEquals("user@email.com", action.email)
    }

    @Test
    fun `Action SetPassword deve conter senha correta`() {
        val action = RegisterContract.Action.SetPassword("123456")
        assertEquals("123456", action.password)
    }

    @Test
    fun `Action SetConfirmPassword deve conter confirmacao correta`() {
        val action = RegisterContract.Action.SetConfirmPassword("123456")
        assertEquals("123456", action.confirmPassword)
    }

    @Test
    fun `Action TogglePasswordVisibility deve existir`() {
        val action = RegisterContract.Action.TogglePasswordVisibility
        assertTrue(action is RegisterContract.Action)
    }

    @Test
    fun `Action ToggleConfirmPasswordVisibility deve existir`() {
        val action = RegisterContract.Action.ToggleConfirmPasswordVisibility
        assertTrue(action is RegisterContract.Action)
    }

    @Test
    fun `Action Register deve existir`() {
        val action = RegisterContract.Action.Register
        assertTrue(action is RegisterContract.Action)
    }

    @Test
    fun `Action NavigateToLogin deve existir`() {
        val action = RegisterContract.Action.NavigateToLogin
        assertTrue(action is RegisterContract.Action)
    }

    @Test
    fun `Action ClearError deve existir`() {
        val action = RegisterContract.Action.ClearError
        assertTrue(action is RegisterContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateToHome deve existir`() {
        val effect = RegisterContract.Effect.NavigateToHome
        assertTrue(effect is RegisterContract.Effect)
    }

    @Test
    fun `Effect NavigateToLogin deve existir`() {
        val effect = RegisterContract.Effect.NavigateToLogin
        assertTrue(effect is RegisterContract.Effect)
    }

    @Test
    fun `Effect ShowSnackbar deve conter mensagem correta`() {
        val effect = RegisterContract.Effect.ShowSnackbar("Cadastro realizado!")
        assertEquals("Cadastro realizado!", effect.message)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - usuario preenchendo formulario passo a passo`() {
        var state = RegisterContract.State()

        // Preenche nome
        state = state.copy(name = "Usuario")
        assertFalse(state.isRegisterEnabled)

        // Preenche email
        state = state.copy(email = "user@email.com")
        assertFalse(state.isRegisterEnabled)

        // Preenche senha curta
        state = state.copy(password = "12345")
        assertFalse(state.isRegisterEnabled)
        assertFalse(state.isPasswordValid)

        // Completa senha
        state = state.copy(password = "123456")
        assertFalse(state.isRegisterEnabled) // Falta confirmação
        assertTrue(state.isPasswordValid)

        // Digita confirmação errada
        state = state.copy(confirmPassword = "654321")
        assertFalse(state.isRegisterEnabled)
        assertFalse(state.doPasswordsMatch)

        // Corrige confirmação
        state = state.copy(confirmPassword = "123456")
        assertTrue(state.isRegisterEnabled)
        assertTrue(state.doPasswordsMatch)
    }

    @Test
    fun `Cenario - toggle password visibility`() {
        var state = RegisterContract.State()

        // Inicialmente invisíveis
        assertFalse(state.isPasswordVisible)
        assertFalse(state.isConfirmPasswordVisible)

        // Toggle senha principal
        state = state.copy(isPasswordVisible = true)
        assertTrue(state.isPasswordVisible)
        assertFalse(state.isConfirmPasswordVisible)

        // Toggle confirmação
        state = state.copy(isConfirmPasswordVisible = true)
        assertTrue(state.isPasswordVisible)
        assertTrue(state.isConfirmPasswordVisible)
    }

    @Test
    fun `Cenario - validacao de email em tempo real`() {
        var state = RegisterContract.State()

        // Email parcial - ainda válido (vazio ou incompleto)
        state = state.copy(email = "user")
        assertFalse(state.isEmailValid)

        // Email com @
        state = state.copy(email = "user@")
        assertFalse(state.isEmailValid)

        // Email completo
        state = state.copy(email = "user@email.com")
        assertTrue(state.isEmailValid)
    }
}
