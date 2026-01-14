package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes unitários para o enum PeriodoLocacao.
 */
class PeriodoLocacaoTest {

    @Test
    fun `PeriodoLocacao DIARIO deve ter 1 dia`() {
        assertEquals(1, PeriodoLocacao.DIARIO.dias)
        assertEquals("Diário", PeriodoLocacao.DIARIO.label)
    }

    @Test
    fun `PeriodoLocacao SEMANAL deve ter 7 dias`() {
        assertEquals(7, PeriodoLocacao.SEMANAL.dias)
        assertEquals("Semanal", PeriodoLocacao.SEMANAL.label)
    }

    @Test
    fun `PeriodoLocacao QUINZENAL deve ter 15 dias`() {
        assertEquals(15, PeriodoLocacao.QUINZENAL.dias)
        assertEquals("Quinzenal", PeriodoLocacao.QUINZENAL.label)
    }

    @Test
    fun `PeriodoLocacao MENSAL deve ter 30 dias`() {
        assertEquals(30, PeriodoLocacao.MENSAL.dias)
        assertEquals("Mensal", PeriodoLocacao.MENSAL.label)
    }

    @Test
    fun `PeriodoLocacao deve ter 4 valores`() {
        assertEquals(4, PeriodoLocacao.entries.size)
    }

    @Test
    fun `PeriodoLocacao entries devem estar na ordem correta`() {
        val entries = PeriodoLocacao.entries
        assertEquals(PeriodoLocacao.DIARIO, entries[0])
        assertEquals(PeriodoLocacao.SEMANAL, entries[1])
        assertEquals(PeriodoLocacao.QUINZENAL, entries[2])
        assertEquals(PeriodoLocacao.MENSAL, entries[3])
    }

    @Test
    fun `PeriodoLocacao dias devem ser crescentes`() {
        val entries = PeriodoLocacao.entries
        for (i in 0 until entries.size - 1) {
            assertTrue(
                entries[i].dias < entries[i + 1].dias,
                "${entries[i].name} deveria ter menos dias que ${entries[i + 1].name}"
            )
        }
    }
}
