package br.com.codecacto.locadora.features.notifications.presentation

import br.com.codecacto.locadora.currentTimeMillis

import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.core.model.NotificacaoTipo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Testes unitários para NotificationsContract - State, Actions e Effects.
 */
class NotificationsContractTest {

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = NotificationsContract.State()

        assertTrue(state.notificacoes.isEmpty())
        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals(0, state.unreadCount)
        assertNull(state.error)
    }

    // ==================== TESTES DE STATE - Lista de Notificacoes ====================

    @Test
    fun `State com notificacoes carregadas`() {
        val notificacoes = listOf(
            Notificacao(
                id = "not-1",
                titulo = "Locação vencendo",
                mensagem = "A locação LOC-123 vence amanhã",
                tipo = NotificacaoTipo.VENCIMENTO.valor,
                lida = false
            ),
            Notificacao(
                id = "not-2",
                titulo = "Pagamento recebido",
                mensagem = "Pagamento da locação LOC-456 confirmado",
                tipo = NotificacaoTipo.PAGAMENTO.valor,
                lida = true
            )
        )

        val state = NotificationsContract.State(
            notificacoes = notificacoes,
            isLoading = false
        )

        assertEquals(2, state.notificacoes.size)
        assertEquals("Locação vencendo", state.notificacoes[0].titulo)
        assertFalse(state.notificacoes[0].lida)
        assertTrue(state.notificacoes[1].lida)
    }

    @Test
    fun `State com contagem de nao lidas`() {
        val notificacoes = listOf(
            Notificacao(id = "not-1", lida = false),
            Notificacao(id = "not-2", lida = false),
            Notificacao(id = "not-3", lida = true)
        )

        val state = NotificationsContract.State(
            notificacoes = notificacoes,
            unreadCount = 2
        )

        assertEquals(3, state.notificacoes.size)
        assertEquals(2, state.unreadCount)
    }

    // ==================== TESTES DE STATE - Loading e Refresh ====================

    @Test
    fun `State durante loading`() {
        val state = NotificationsContract.State(isLoading = true)

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `State durante refresh`() {
        val state = NotificationsContract.State(
            isLoading = false,
            isRefreshing = true
        )

        assertFalse(state.isLoading)
        assertTrue(state.isRefreshing)
    }

    @Test
    fun `State com erro`() {
        val state = NotificationsContract.State(
            error = "Erro ao carregar notificações"
        )

        assertEquals("Erro ao carregar notificações", state.error)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action OnNotificacaoClick deve conter notificacao correta`() {
        val notificacao = Notificacao(
            id = "not-123",
            titulo = "Nova entrega",
            mensagem = "Entrega agendada para hoje",
            tipo = NotificacaoTipo.ENTREGA.valor
        )
        val action = NotificationsContract.Action.OnNotificacaoClick(notificacao)

        assertEquals("not-123", action.notificacao.id)
        assertEquals("Nova entrega", action.notificacao.titulo)
    }

    @Test
    fun `Action OnMarcarComoLida deve conter id correto`() {
        val action = NotificationsContract.Action.OnMarcarComoLida("not-123")

        assertEquals("not-123", action.id)
    }

    @Test
    fun `Action OnExcluir deve conter id correto`() {
        val action = NotificationsContract.Action.OnExcluir("not-456")

        assertEquals("not-456", action.id)
    }

    @Test
    fun `Action OnMarcarTodasComoLidas deve existir`() {
        val action = NotificationsContract.Action.OnMarcarTodasComoLidas
        assertTrue(action is NotificationsContract.Action)
    }

    @Test
    fun `Action OnLimparLidas deve existir`() {
        val action = NotificationsContract.Action.OnLimparLidas
        assertTrue(action is NotificationsContract.Action)
    }

    @Test
    fun `Action Refresh deve existir`() {
        val action = NotificationsContract.Action.Refresh
        assertTrue(action is NotificationsContract.Action)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect NavigateBack deve existir`() {
        val effect = NotificationsContract.Effect.NavigateBack
        assertTrue(effect is NotificationsContract.Effect)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = NotificationsContract.Effect.ShowError("Erro ao excluir notificação")

        assertEquals("Erro ao excluir notificação", effect.message)
    }

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = NotificationsContract.Effect.ShowSuccess("Notificação excluída")

        assertEquals("Notificação excluída", effect.message)
    }

    // ==================== TESTES DE Notificacao Model ====================

    @Test
    fun `Notificacao valores padrao`() {
        val notificacao = Notificacao()

        assertEquals("", notificacao.id)
        assertEquals("", notificacao.titulo)
        assertEquals("", notificacao.mensagem)
        assertEquals(NotificacaoTipo.INFO.valor, notificacao.tipo)
        assertFalse(notificacao.lida)
        assertTrue(notificacao.dados.isEmpty())
    }

    @Test
    fun `Notificacao com dados customizados`() {
        val now = currentTimeMillis()
        val notificacao = Notificacao(
            id = "not-123",
            titulo = "Locação vencendo",
            mensagem = "A locação LOC-456 vence em 2 dias",
            tipo = NotificacaoTipo.VENCIMENTO.valor,
            lida = false,
            dados = mapOf("locacaoId" to "LOC-456", "diasRestantes" to "2"),
            criadoEm = now
        )

        assertEquals("not-123", notificacao.id)
        assertEquals("Locação vencendo", notificacao.titulo)
        assertEquals(NotificacaoTipo.VENCIMENTO.valor, notificacao.tipo)
        assertEquals("LOC-456", notificacao.dados["locacaoId"])
        assertEquals("2", notificacao.dados["diasRestantes"])
    }

    @Test
    fun `Notificacao COLLECTION_NAME deve ser correto`() {
        assertEquals("notificacoes", Notificacao.COLLECTION_NAME)
    }

    // ==================== TESTES DE NotificacaoTipo ====================

    @Test
    fun `NotificacaoTipo deve ter 6 valores`() {
        assertEquals(6, NotificacaoTipo.entries.size)
    }

    @Test
    fun `NotificacaoTipo valores devem estar corretos`() {
        assertEquals("info", NotificacaoTipo.INFO.valor)
        assertEquals("locacao", NotificacaoTipo.LOCACAO.valor)
        assertEquals("entrega", NotificacaoTipo.ENTREGA.valor)
        assertEquals("pagamento", NotificacaoTipo.PAGAMENTO.valor)
        assertEquals("vencimento", NotificacaoTipo.VENCIMENTO.valor)
        assertEquals("sistema", NotificacaoTipo.SISTEMA.valor)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - carregar notificacoes`() {
        var state = NotificationsContract.State()

        // Inicia loading
        assertTrue(state.isLoading)

        // Dados carregados
        val notificacoes = listOf(
            Notificacao(id = "not-1", titulo = "Alerta 1", lida = false),
            Notificacao(id = "not-2", titulo = "Alerta 2", lida = false),
            Notificacao(id = "not-3", titulo = "Info", lida = true)
        )

        state = state.copy(
            isLoading = false,
            notificacoes = notificacoes,
            unreadCount = 2
        )

        assertFalse(state.isLoading)
        assertEquals(3, state.notificacoes.size)
        assertEquals(2, state.unreadCount)
    }

    @Test
    fun `Cenario - marcar notificacao como lida`() {
        val notificacoesIniciais = listOf(
            Notificacao(id = "not-1", titulo = "Alerta", lida = false),
            Notificacao(id = "not-2", titulo = "Info", lida = true)
        )

        var state = NotificationsContract.State(
            isLoading = false,
            notificacoes = notificacoesIniciais,
            unreadCount = 1
        )

        // Marca como lida
        val notificacoesAtualizadas = notificacoesIniciais.map {
            if (it.id == "not-1") it.copy(lida = true) else it
        }

        state = state.copy(
            notificacoes = notificacoesAtualizadas,
            unreadCount = 0
        )

        assertTrue(state.notificacoes[0].lida)
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `Cenario - marcar todas como lidas`() {
        val notificacoesIniciais = listOf(
            Notificacao(id = "not-1", lida = false),
            Notificacao(id = "not-2", lida = false),
            Notificacao(id = "not-3", lida = false)
        )

        var state = NotificationsContract.State(
            isLoading = false,
            notificacoes = notificacoesIniciais,
            unreadCount = 3
        )

        // Marca todas como lidas
        val notificacoesLidas = notificacoesIniciais.map { it.copy(lida = true) }

        state = state.copy(
            notificacoes = notificacoesLidas,
            unreadCount = 0
        )

        assertTrue(state.notificacoes.all { it.lida })
        assertEquals(0, state.unreadCount)
    }

    @Test
    fun `Cenario - excluir notificacao`() {
        val notificacoesIniciais = listOf(
            Notificacao(id = "not-1", titulo = "Alerta 1"),
            Notificacao(id = "not-2", titulo = "Alerta 2")
        )

        var state = NotificationsContract.State(
            isLoading = false,
            notificacoes = notificacoesIniciais
        )

        assertEquals(2, state.notificacoes.size)

        // Exclui notificação
        val notificacoesFiltradas = notificacoesIniciais.filter { it.id != "not-1" }

        state = state.copy(notificacoes = notificacoesFiltradas)

        assertEquals(1, state.notificacoes.size)
        assertEquals("not-2", state.notificacoes[0].id)
    }

    @Test
    fun `Cenario - limpar notificacoes lidas`() {
        val notificacoes = listOf(
            Notificacao(id = "not-1", lida = false),
            Notificacao(id = "not-2", lida = true),
            Notificacao(id = "not-3", lida = true)
        )

        var state = NotificationsContract.State(
            isLoading = false,
            notificacoes = notificacoes,
            unreadCount = 1
        )

        // Limpa lidas
        val naoLidas = notificacoes.filter { !it.lida }

        state = state.copy(notificacoes = naoLidas)

        assertEquals(1, state.notificacoes.size)
        assertEquals("not-1", state.notificacoes[0].id)
    }

    @Test
    fun `Cenario - notificacao com dados de navegacao`() {
        val notificacao = Notificacao(
            id = "not-1",
            titulo = "Pagamento pendente",
            mensagem = "A locação LOC-789 está com pagamento pendente",
            tipo = NotificacaoTipo.PAGAMENTO.valor,
            dados = mapOf(
                "locacaoId" to "LOC-789",
                "clienteId" to "CLI-123",
                "action" to "open_locacao"
            )
        )

        assertEquals("LOC-789", notificacao.dados["locacaoId"])
        assertEquals("open_locacao", notificacao.dados["action"])
    }

    @Test
    fun `Cenario - refresh com novas notificacoes`() {
        val notificacoesAnteriores = listOf(
            Notificacao(id = "not-1", titulo = "Antiga")
        )

        var state = NotificationsContract.State(
            isLoading = false,
            notificacoes = notificacoesAnteriores,
            unreadCount = 1
        )

        // Inicia refresh
        state = state.copy(isRefreshing = true)
        assertTrue(state.isRefreshing)

        // Recebe novas notificações
        val novasNotificacoes = listOf(
            Notificacao(id = "not-2", titulo = "Nova", lida = false),
            Notificacao(id = "not-1", titulo = "Antiga", lida = true)
        )

        state = state.copy(
            isRefreshing = false,
            notificacoes = novasNotificacoes,
            unreadCount = 1
        )

        assertFalse(state.isRefreshing)
        assertEquals(2, state.notificacoes.size)
    }
}
