package br.com.codecacto.locadora.features.feedback.presentation

import br.com.codecacto.locadora.core.model.FeedbackMotivo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para FeedbackContract - State, Actions e Effects.
 */
class FeedbackContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = FeedbackContract.State()

        assertNull(state.selectedMotivo)
        assertEquals("", state.mensagem)
        assertFalse(state.isLoading)
        assertFalse(state.isSent)
        assertNull(state.motivoError)
        assertNull(state.mensagemError)
    }

    // ==================== TESTES DE STATE - Motivo ====================

    @Test
    fun `State com motivo SUGESTAO selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.SUGESTAO)

        assertNotNull(state.selectedMotivo)
        assertEquals(FeedbackMotivo.SUGESTAO, state.selectedMotivo)
        assertEquals("sugestao", state.selectedMotivo!!.valor)
        assertEquals("Sugestao", state.selectedMotivo!!.label)
    }

    @Test
    fun `State com motivo BUG selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.BUG)

        assertEquals(FeedbackMotivo.BUG, state.selectedMotivo)
        assertEquals("bug", state.selectedMotivo!!.valor)
        assertEquals("Reportar Bug", state.selectedMotivo!!.label)
    }

    @Test
    fun `State com motivo RECLAMACAO selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.RECLAMACAO)

        assertEquals(FeedbackMotivo.RECLAMACAO, state.selectedMotivo)
        assertEquals("reclamacao", state.selectedMotivo!!.valor)
    }

    @Test
    fun `State com motivo DUVIDA selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.DUVIDA)

        assertEquals(FeedbackMotivo.DUVIDA, state.selectedMotivo)
        assertEquals("duvida", state.selectedMotivo!!.valor)
    }

    @Test
    fun `State com motivo ELOGIO selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.ELOGIO)

        assertEquals(FeedbackMotivo.ELOGIO, state.selectedMotivo)
        assertEquals("elogio", state.selectedMotivo!!.valor)
    }

    @Test
    fun `State com motivo OUTRO selecionado`() {
        val state = FeedbackContract.State(selectedMotivo = FeedbackMotivo.OUTRO)

        assertEquals(FeedbackMotivo.OUTRO, state.selectedMotivo)
        assertEquals("outro", state.selectedMotivo!!.valor)
    }

    // ==================== TESTES DE STATE - Mensagem ====================

    @Test
    fun `State com mensagem preenchida`() {
        val state = FeedbackContract.State(
            mensagem = "O aplicativo é muito bom, mas poderia ter modo escuro."
        )

        assertEquals("O aplicativo é muito bom, mas poderia ter modo escuro.", state.mensagem)
    }

    @Test
    fun `State com mensagem longa`() {
        val mensagemLonga = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris."

        val state = FeedbackContract.State(mensagem = mensagemLonga)

        assertEquals(mensagemLonga, state.mensagem)
        assertTrue(state.mensagem.length > 100)
    }

    // ==================== TESTES DE STATE - Loading e Sent ====================

    @Test
    fun `State durante envio`() {
        val state = FeedbackContract.State(isLoading = true)

        assertTrue(state.isLoading)
        assertFalse(state.isSent)
    }

    @Test
    fun `State apos envio com sucesso`() {
        val state = FeedbackContract.State(
            isLoading = false,
            isSent = true
        )

        assertFalse(state.isLoading)
        assertTrue(state.isSent)
    }

    // ==================== TESTES DE STATE - Erros ====================

    @Test
    fun `State com erro de motivo`() {
        val state = FeedbackContract.State(
            motivoError = "Selecione um motivo"
        )

        assertEquals("Selecione um motivo", state.motivoError)
        assertNull(state.mensagemError)
    }

    @Test
    fun `State com erro de mensagem`() {
        val state = FeedbackContract.State(
            mensagemError = "A mensagem deve ter pelo menos 10 caracteres"
        )

        assertNull(state.motivoError)
        assertEquals("A mensagem deve ter pelo menos 10 caracteres", state.mensagemError)
    }

    @Test
    fun `State com ambos os erros`() {
        val state = FeedbackContract.State(
            motivoError = "Selecione um motivo",
            mensagemError = "Mensagem obrigatória"
        )

        assertNotNull(state.motivoError)
        assertNotNull(state.mensagemError)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action OnMotivoSelected deve conter motivo correto`() {
        val action = FeedbackContract.Action.OnMotivoSelected(FeedbackMotivo.BUG)

        assertEquals(FeedbackMotivo.BUG, action.motivo)
    }

    @Test
    fun `Action OnMensagemChanged deve conter mensagem correta`() {
        val action = FeedbackContract.Action.OnMensagemChanged("Feedback de teste")

        assertEquals("Feedback de teste", action.mensagem)
    }

    @Test
    fun `Action OnSendClick deve existir`() {
        val action = FeedbackContract.Action.OnSendClick
        assertTrue(action is FeedbackContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateBack deve existir`() {
        val effect = FeedbackContract.Effect.NavigateBack
        assertTrue(effect is FeedbackContract.Effect)
    }

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = FeedbackContract.Effect.ShowSuccess("Feedback enviado com sucesso!")

        assertEquals("Feedback enviado com sucesso!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = FeedbackContract.Effect.ShowError("Erro ao enviar feedback")

        assertEquals("Erro ao enviar feedback", effect.message)
    }

    // ==================== TESTES DE FeedbackMotivo ====================

    @Test
    fun `FeedbackMotivo deve ter 6 valores`() {
        assertEquals(6, FeedbackMotivo.entries.size)
    }

    @Test
    fun `FeedbackMotivo valores e labels devem estar corretos`() {
        assertEquals("sugestao", FeedbackMotivo.SUGESTAO.valor)
        assertEquals("Sugestao", FeedbackMotivo.SUGESTAO.label)

        assertEquals("bug", FeedbackMotivo.BUG.valor)
        assertEquals("Reportar Bug", FeedbackMotivo.BUG.label)

        assertEquals("reclamacao", FeedbackMotivo.RECLAMACAO.valor)
        assertEquals("Reclamacao", FeedbackMotivo.RECLAMACAO.label)

        assertEquals("duvida", FeedbackMotivo.DUVIDA.valor)
        assertEquals("Duvida", FeedbackMotivo.DUVIDA.label)

        assertEquals("elogio", FeedbackMotivo.ELOGIO.valor)
        assertEquals("Elogio", FeedbackMotivo.ELOGIO.label)

        assertEquals("outro", FeedbackMotivo.OUTRO.valor)
        assertEquals("Outro", FeedbackMotivo.OUTRO.label)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - preencher formulario de feedback completo`() {
        var state = FeedbackContract.State()

        // Seleciona motivo
        state = state.copy(selectedMotivo = FeedbackMotivo.SUGESTAO)
        assertNotNull(state.selectedMotivo)

        // Digita mensagem
        state = state.copy(mensagem = "Adicionar modo escuro ao aplicativo")
        assertEquals("Adicionar modo escuro ao aplicativo", state.mensagem)

        // Inicia envio
        state = state.copy(isLoading = true)
        assertTrue(state.isLoading)

        // Envio concluído
        state = state.copy(isLoading = false, isSent = true)
        assertFalse(state.isLoading)
        assertTrue(state.isSent)
    }

    @Test
    fun `Cenario - validacao de formulario com erros`() {
        var state = FeedbackContract.State()

        // Tenta enviar sem preencher
        state = state.copy(
            motivoError = "Selecione um motivo",
            mensagemError = "Digite sua mensagem"
        )

        assertNotNull(state.motivoError)
        assertNotNull(state.mensagemError)

        // Seleciona motivo - limpa erro
        state = state.copy(
            selectedMotivo = FeedbackMotivo.BUG,
            motivoError = null
        )
        assertNull(state.motivoError)

        // Digita mensagem - limpa erro
        state = state.copy(
            mensagem = "O botão X não funciona",
            mensagemError = null
        )
        assertNull(state.mensagemError)
    }

    @Test
    fun `Cenario - reportar bug`() {
        var state = FeedbackContract.State()

        state = state.copy(
            selectedMotivo = FeedbackMotivo.BUG,
            mensagem = "O aplicativo fecha ao clicar em 'Nova Locação'"
        )

        assertEquals(FeedbackMotivo.BUG, state.selectedMotivo)
        assertTrue(state.mensagem.contains("Nova Locação"))
    }

    @Test
    fun `Cenario - enviar elogio`() {
        var state = FeedbackContract.State()

        state = state.copy(
            selectedMotivo = FeedbackMotivo.ELOGIO,
            mensagem = "Excelente aplicativo! Facilita muito meu trabalho."
        )

        assertEquals(FeedbackMotivo.ELOGIO, state.selectedMotivo)
        assertTrue(state.mensagem.isNotEmpty())
    }

    @Test
    fun `Cenario - erro ao enviar feedback`() {
        var state = FeedbackContract.State(
            selectedMotivo = FeedbackMotivo.SUGESTAO,
            mensagem = "Adicionar exportação para Excel",
            isLoading = true
        )

        // Simula erro de rede
        state = state.copy(isLoading = false)
        // Effect ShowError seria disparado

        assertFalse(state.isLoading)
        assertFalse(state.isSent) // Não foi enviado com sucesso
    }
}
