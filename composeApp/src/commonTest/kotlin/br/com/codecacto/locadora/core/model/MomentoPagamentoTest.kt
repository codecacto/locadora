package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Testes unitários para o enum MomentoPagamento.
 */
class MomentoPagamentoTest {

    @Test
    fun `MomentoPagamento NO_INICIO deve ter label correto`() {
        assertEquals("No Início", MomentoPagamento.NO_INICIO.label)
    }

    @Test
    fun `MomentoPagamento NO_VENCIMENTO deve ter label correto`() {
        assertEquals("No Vencimento", MomentoPagamento.NO_VENCIMENTO.label)
    }

    @Test
    fun `MomentoPagamento deve ter 2 valores`() {
        assertEquals(2, MomentoPagamento.entries.size)
    }

    @Test
    fun `MomentoPagamento entries devem estar na ordem correta`() {
        val entries = MomentoPagamento.entries
        assertEquals(MomentoPagamento.NO_INICIO, entries[0])
        assertEquals(MomentoPagamento.NO_VENCIMENTO, entries[1])
    }
}
