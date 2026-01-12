package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para a classe Recebimento.
 */
class RecebimentoTest {

    // ==================== TESTES DE Recebimento - Valores Padrao ====================

    @Test
    fun `Recebimento com valores padrao deve ter campos corretos`() {
        val recebimento = Recebimento()

        assertEquals("", recebimento.id)
        assertEquals("", recebimento.locacaoId)
        assertEquals("", recebimento.clienteId)
        assertEquals("", recebimento.equipamentoId)
        assertEquals(0.0, recebimento.valor)
        assertEquals(StatusPagamento.PENDENTE, recebimento.status)
        assertNull(recebimento.dataPagamento)
        assertEquals(0, recebimento.numeroRenovacao)
    }

    @Test
    fun `Recebimento COLLECTION_NAME deve ser correto`() {
        assertEquals("recebimentos", Recebimento.COLLECTION_NAME)
    }

    // ==================== TESTES DE Recebimento - Criacao ====================

    @Test
    fun `Recebimento criado com dados deve manter valores`() {
        val now = System.currentTimeMillis()
        val vencimento = now + (7 * 24 * 60 * 60 * 1000L) // 7 dias

        val recebimento = Recebimento(
            id = "rec-123",
            locacaoId = "loc-456",
            clienteId = "cli-789",
            equipamentoId = "equip-012",
            valor = 500.0,
            dataVencimento = vencimento,
            status = StatusPagamento.PENDENTE,
            numeroRenovacao = 0
        )

        assertEquals("rec-123", recebimento.id)
        assertEquals("loc-456", recebimento.locacaoId)
        assertEquals("cli-789", recebimento.clienteId)
        assertEquals("equip-012", recebimento.equipamentoId)
        assertEquals(500.0, recebimento.valor)
        assertEquals(vencimento, recebimento.dataVencimento)
        assertEquals(StatusPagamento.PENDENTE, recebimento.status)
        assertEquals(0, recebimento.numeroRenovacao)
    }

    // ==================== TESTES DE Recebimento - Status ====================

    @Test
    fun `Recebimento pendente deve ter status PENDENTE`() {
        val recebimento = Recebimento(status = StatusPagamento.PENDENTE)
        assertEquals(StatusPagamento.PENDENTE, recebimento.status)
    }

    @Test
    fun `Recebimento pago deve ter status PAGO`() {
        val recebimento = Recebimento(status = StatusPagamento.PAGO)
        assertEquals(StatusPagamento.PAGO, recebimento.status)
    }

    @Test
    fun `Recebimento transicao PENDENTE para PAGO deve funcionar`() {
        val pendente = Recebimento(status = StatusPagamento.PENDENTE)
        val pago = pendente.copy(
            status = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )

        assertEquals(StatusPagamento.PENDENTE, pendente.status)
        assertEquals(StatusPagamento.PAGO, pago.status)
        assertNotNull(pago.dataPagamento)
    }

    // ==================== TESTES DE Recebimento - Renovacao ====================

    @Test
    fun `Recebimento original deve ter numeroRenovacao 0`() {
        val recebimento = Recebimento(numeroRenovacao = 0)
        assertEquals(0, recebimento.numeroRenovacao)
    }

    @Test
    fun `Recebimento primeira renovacao deve ter numeroRenovacao 1`() {
        val recebimento = Recebimento(numeroRenovacao = 1)
        assertEquals(1, recebimento.numeroRenovacao)
    }

    @Test
    fun `Recebimento multiplas renovacoes deve ter numeroRenovacao correto`() {
        val recebimento = Recebimento(numeroRenovacao = 5)
        assertEquals(5, recebimento.numeroRenovacao)
    }

    // ==================== TESTES DE Recebimento - Valor ====================

    @Test
    fun `Recebimento com valor positivo deve manter valor`() {
        val recebimento = Recebimento(valor = 1500.50)
        assertEquals(1500.50, recebimento.valor)
    }

    @Test
    fun `Recebimento com valor zero deve manter valor`() {
        val recebimento = Recebimento(valor = 0.0)
        assertEquals(0.0, recebimento.valor)
    }

    @Test
    fun `Recebimento com valor grande deve manter valor`() {
        val recebimento = Recebimento(valor = 99999.99)
        assertEquals(99999.99, recebimento.valor)
    }

    // ==================== TESTES DE Recebimento - Copy ====================

    @Test
    fun `Recebimento copy deve criar nova instancia com valores alterados`() {
        val original = Recebimento(
            id = "rec-123",
            valor = 500.0,
            status = StatusPagamento.PENDENTE
        )

        val atualizado = original.copy(
            status = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )

        // Original deve permanecer inalterado
        assertEquals(StatusPagamento.PENDENTE, original.status)
        assertNull(original.dataPagamento)

        // Cópia deve ter valores atualizados
        assertEquals(StatusPagamento.PAGO, atualizado.status)
        assertNotNull(atualizado.dataPagamento)

        // Valores não alterados devem ser mantidos
        assertEquals(original.id, atualizado.id)
        assertEquals(original.valor, atualizado.valor)
    }

    // ==================== TESTES DE Recebimento - Relacionamentos ====================

    @Test
    fun `Recebimento deve manter referencia a locacao`() {
        val recebimento = Recebimento(
            locacaoId = "loc-123",
            clienteId = "cli-456",
            equipamentoId = "equip-789"
        )

        assertEquals("loc-123", recebimento.locacaoId)
        assertEquals("cli-456", recebimento.clienteId)
        assertEquals("equip-789", recebimento.equipamentoId)
    }
}
