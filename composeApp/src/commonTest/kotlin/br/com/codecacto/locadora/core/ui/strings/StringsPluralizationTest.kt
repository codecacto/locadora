package br.com.codecacto.locadora.core.ui.strings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes unitários para funções de pluralização em Strings.
 */
class StringsPluralizationTest {

    // ==================== TESTES DE LOCACOES ATIVAS ====================

    @Test
    fun `locacoesAtivas - singular para 1 locacao`() {
        val resultado = Strings.locacoesAtivas(1)
        assertEquals("1 locação ativa", resultado)
    }

    @Test
    fun `locacoesAtivas - plural para 0 locacoes`() {
        val resultado = Strings.locacoesAtivas(0)
        assertEquals("0 locações ativas", resultado)
    }

    @Test
    fun `locacoesAtivas - plural para multiplas locacoes`() {
        assertEquals("2 locações ativas", Strings.locacoesAtivas(2))
        assertEquals("10 locações ativas", Strings.locacoesAtivas(10))
        assertEquals("100 locações ativas", Strings.locacoesAtivas(100))
    }

    // ==================== TESTES DE LOCACOES FINALIZADAS ====================

    @Test
    fun `locacoesFinalizadas - singular para 1 locacao`() {
        val resultado = Strings.locacoesFinalizadas(1)
        assertEquals("1 locação finalizada", resultado)
    }

    @Test
    fun `locacoesFinalizadas - plural para multiplas locacoes`() {
        assertEquals("0 locações finalizadas", Strings.locacoesFinalizadas(0))
        assertEquals("5 locações finalizadas", Strings.locacoesFinalizadas(5))
    }

    // ==================== TESTES DE ENTREGAS PENDENTES ====================

    @Test
    fun `entregasPendentes - singular para 1 entrega`() {
        val resultado = Strings.entregasPendentes(1)
        assertEquals("1 entrega pendente", resultado)
    }

    @Test
    fun `entregasPendentes - plural para multiplas entregas`() {
        assertEquals("0 entregas pendentes", Strings.entregasPendentes(0))
        assertEquals("3 entregas pendentes", Strings.entregasPendentes(3))
    }

    // ==================== TESTES DE RECEBIMENTOS PENDENTES ====================

    @Test
    fun `recebimentosPendentes - singular para 1 recebimento`() {
        val resultado = Strings.recebimentosPendentes(1)
        assertEquals("1 pagamento pendente", resultado)
    }

    @Test
    fun `recebimentosPendentes - plural para multiplos recebimentos`() {
        assertEquals("0 pagamentos pendentes", Strings.recebimentosPendentes(0))
        assertEquals("7 pagamentos pendentes", Strings.recebimentosPendentes(7))
    }

    // ==================== TESTES DE RECEBIMENTOS PAGOS ====================

    @Test
    fun `recebimentosPagos - singular para 1 recebimento`() {
        val resultado = Strings.recebimentosPagos(1)
        assertEquals("1 pagamento recebido", resultado)
    }

    @Test
    fun `recebimentosPagos - plural para multiplos recebimentos`() {
        assertEquals("0 pagamentos recebidos", Strings.recebimentosPagos(0))
        assertEquals("15 pagamentos recebidos", Strings.recebimentosPagos(15))
    }

    // ==================== TESTES DE LOCACOES PAGAS ====================

    @Test
    fun `locacoesPagas - singular para 1 locacao`() {
        val resultado = Strings.locacoesPagas(1)
        assertEquals("1 locação paga", resultado)
    }

    @Test
    fun `locacoesPagas - plural para 0 locacoes`() {
        val resultado = Strings.locacoesPagas(0)
        assertEquals("0 locações pagas", resultado)
    }

    @Test
    fun `locacoesPagas - plural para multiplas locacoes`() {
        assertEquals("2 locações pagas", Strings.locacoesPagas(2))
        assertEquals("50 locações pagas", Strings.locacoesPagas(50))
    }

    // ==================== TESTES DE CLIENTES CADASTRADOS ====================

    @Test
    fun `clientesCadastrados - singular para 1 cliente`() {
        val resultado = Strings.clientesCadastrados(1)
        assertEquals("1 cliente cadastrado", resultado)
    }

    @Test
    fun `clientesCadastrados - plural para 0 clientes`() {
        val resultado = Strings.clientesCadastrados(0)
        assertEquals("0 clientes cadastrados", resultado)
    }

    @Test
    fun `clientesCadastrados - plural para multiplos clientes`() {
        assertEquals("2 clientes cadastrados", Strings.clientesCadastrados(2))
        assertEquals("25 clientes cadastrados", Strings.clientesCadastrados(25))
    }

    // ==================== TESTES DE EQUIPAMENTOS CADASTRADOS ====================

    @Test
    fun `equipamentosCadastrados - singular para 1 equipamento`() {
        val resultado = Strings.equipamentosCadastrados(1)
        assertEquals("1 equipamento cadastrado", resultado)
    }

    @Test
    fun `equipamentosCadastrados - plural para 0 equipamentos`() {
        val resultado = Strings.equipamentosCadastrados(0)
        assertEquals("0 equipamentos cadastrados", resultado)
    }

    @Test
    fun `equipamentosCadastrados - plural para multiplos equipamentos`() {
        assertEquals("2 equipamentos cadastrados", Strings.equipamentosCadastrados(2))
        assertEquals("30 equipamentos cadastrados", Strings.equipamentosCadastrados(30))
    }

    // ==================== TESTES DE FORMATO OUTROS PERIODOS ====================

    @Test
    fun `formatOutrosPeriodos - singular para 1 outro`() {
        val resultado = Strings.formatOutrosPeriodos(1)
        assertEquals("+1 outro", resultado)
    }

    @Test
    fun `formatOutrosPeriodos - plural para multiplos outros`() {
        assertEquals("+2 outros", Strings.formatOutrosPeriodos(2))
        assertEquals("+5 outros", Strings.formatOutrosPeriodos(5))
    }

    // ==================== TESTES DE CONSISTENCIA ====================

    @Test
    fun `todas as funcoes de pluralizacao devem seguir o mesmo padrao`() {
        // Verifica que todas as funções retornam singular para count = 1
        assertTrue(Strings.locacoesAtivas(1).contains("locação ativa"))
        assertTrue(Strings.locacoesFinalizadas(1).contains("locação finalizada"))
        assertTrue(Strings.entregasPendentes(1).contains("entrega pendente"))
        assertTrue(Strings.recebimentosPendentes(1).contains("pagamento pendente"))
        assertTrue(Strings.recebimentosPagos(1).contains("pagamento recebido"))
        assertTrue(Strings.locacoesPagas(1).contains("locação paga"))
        assertTrue(Strings.clientesCadastrados(1).contains("cliente cadastrado"))
        assertTrue(Strings.equipamentosCadastrados(1).contains("equipamento cadastrado"))
    }

    @Test
    fun `todas as funcoes de pluralizacao devem usar plural para count diferente de 1`() {
        // Verifica que todas as funções retornam plural para count != 1
        assertTrue(Strings.locacoesAtivas(2).contains("locações ativas"))
        assertTrue(Strings.locacoesFinalizadas(0).contains("locações finalizadas"))
        assertTrue(Strings.entregasPendentes(5).contains("entregas pendentes"))
        assertTrue(Strings.recebimentosPendentes(3).contains("pagamentos pendentes"))
        assertTrue(Strings.recebimentosPagos(10).contains("pagamentos recebidos"))
        assertTrue(Strings.locacoesPagas(7).contains("locações pagas"))
        assertTrue(Strings.clientesCadastrados(15).contains("clientes cadastrados"))
        assertTrue(Strings.equipamentosCadastrados(20).contains("equipamentos cadastrados"))
    }
}
