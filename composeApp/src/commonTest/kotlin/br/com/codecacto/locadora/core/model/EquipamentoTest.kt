package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para a classe Equipamento.
 */
class EquipamentoTest {

    // ==================== TESTES DE getPreco ====================

    @Test
    fun `getPreco - periodo DIARIO deve retornar precoDiario`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        assertEquals(50.0, equipamento.getPreco(PeriodoLocacao.DIARIO))
    }

    @Test
    fun `getPreco - periodo SEMANAL deve retornar precoSemanal`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        assertEquals(200.0, equipamento.getPreco(PeriodoLocacao.SEMANAL))
    }

    @Test
    fun `getPreco - periodo QUINZENAL deve retornar precoQuinzenal`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        assertEquals(350.0, equipamento.getPreco(PeriodoLocacao.QUINZENAL))
    }

    @Test
    fun `getPreco - periodo MENSAL deve retornar precoMensal`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        assertEquals(600.0, equipamento.getPreco(PeriodoLocacao.MENSAL))
    }

    @Test
    fun `getPreco - periodo sem preco definido deve retornar null`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        assertNull(equipamento.getPreco(PeriodoLocacao.SEMANAL))
        assertNull(equipamento.getPreco(PeriodoLocacao.QUINZENAL))
        assertNull(equipamento.getPreco(PeriodoLocacao.MENSAL))
    }

    // ==================== TESTES DE getPeriodosDisponiveis ====================

    @Test
    fun `getPeriodosDisponiveis - todos precos preenchidos deve retornar 4 periodos`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertEquals(4, periodos.size)
        assertTrue(periodos.contains(PeriodoLocacao.DIARIO))
        assertTrue(periodos.contains(PeriodoLocacao.SEMANAL))
        assertTrue(periodos.contains(PeriodoLocacao.QUINZENAL))
        assertTrue(periodos.contains(PeriodoLocacao.MENSAL))
    }

    @Test
    fun `getPeriodosDisponiveis - apenas preco diario deve retornar 1 periodo`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertEquals(1, periodos.size)
        assertTrue(periodos.contains(PeriodoLocacao.DIARIO))
    }

    @Test
    fun `getPeriodosDisponiveis - apenas preco mensal deve retornar 1 periodo`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = 600.0
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertEquals(1, periodos.size)
        assertTrue(periodos.contains(PeriodoLocacao.MENSAL))
    }

    @Test
    fun `getPeriodosDisponiveis - precos zerados nao devem ser incluidos`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 0.0,
            precoSemanal = 200.0,
            precoQuinzenal = 0.0,
            precoMensal = 600.0
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertEquals(2, periodos.size)
        assertFalse(periodos.contains(PeriodoLocacao.DIARIO))
        assertTrue(periodos.contains(PeriodoLocacao.SEMANAL))
        assertFalse(periodos.contains(PeriodoLocacao.QUINZENAL))
        assertTrue(periodos.contains(PeriodoLocacao.MENSAL))
    }

    @Test
    fun `getPeriodosDisponiveis - nenhum preco definido deve retornar lista vazia`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertTrue(periodos.isEmpty())
    }

    @Test
    fun `getPeriodosDisponiveis - precos negativos nao devem ser incluidos`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = -50.0,
            precoSemanal = 200.0,
            precoQuinzenal = -100.0,
            precoMensal = 600.0
        )

        val periodos = equipamento.getPeriodosDisponiveis()

        assertEquals(2, periodos.size)
        assertFalse(periodos.contains(PeriodoLocacao.DIARIO))
        assertTrue(periodos.contains(PeriodoLocacao.SEMANAL))
    }

    // ==================== TESTES DE hasAtLeastOnePrice ====================

    @Test
    fun `hasAtLeastOnePrice - todos precos definidos deve retornar true`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        assertTrue(equipamento.hasAtLeastOnePrice())
    }

    @Test
    fun `hasAtLeastOnePrice - apenas preco diario definido deve retornar true`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        assertTrue(equipamento.hasAtLeastOnePrice())
    }

    @Test
    fun `hasAtLeastOnePrice - apenas preco mensal definido deve retornar true`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = 600.0
        )

        assertTrue(equipamento.hasAtLeastOnePrice())
    }

    @Test
    fun `hasAtLeastOnePrice - nenhum preco definido deve retornar false`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        assertFalse(equipamento.hasAtLeastOnePrice())
    }

    @Test
    fun `hasAtLeastOnePrice - precos zerados deve retornar false`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 0.0,
            precoSemanal = 0.0,
            precoQuinzenal = 0.0,
            precoMensal = 0.0
        )

        assertFalse(equipamento.hasAtLeastOnePrice())
    }

    @Test
    fun `hasAtLeastOnePrice - um preco zerado e um valido deve retornar true`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 0.0,
            precoSemanal = 200.0,
            precoQuinzenal = null,
            precoMensal = null
        )

        assertTrue(equipamento.hasAtLeastOnePrice())
    }

    // ==================== TESTES DE getPrimeiroPrecoDisponivel ====================

    @Test
    fun `getPrimeiroPrecoDisponivel - todos precos definidos deve retornar DIARIO primeiro`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 50.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        val resultado = equipamento.getPrimeiroPrecoDisponivel()

        assertNotNull(resultado)
        assertEquals(PeriodoLocacao.DIARIO, resultado.first)
        assertEquals(50.0, resultado.second)
    }

    @Test
    fun `getPrimeiroPrecoDisponivel - sem diario deve retornar SEMANAL`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        val resultado = equipamento.getPrimeiroPrecoDisponivel()

        assertNotNull(resultado)
        assertEquals(PeriodoLocacao.SEMANAL, resultado.first)
        assertEquals(200.0, resultado.second)
    }

    @Test
    fun `getPrimeiroPrecoDisponivel - apenas mensal deve retornar MENSAL`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = 600.0
        )

        val resultado = equipamento.getPrimeiroPrecoDisponivel()

        assertNotNull(resultado)
        assertEquals(PeriodoLocacao.MENSAL, resultado.first)
        assertEquals(600.0, resultado.second)
    }

    @Test
    fun `getPrimeiroPrecoDisponivel - nenhum preco disponivel deve retornar null`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = null,
            precoSemanal = null,
            precoQuinzenal = null,
            precoMensal = null
        )

        assertNull(equipamento.getPrimeiroPrecoDisponivel())
    }

    @Test
    fun `getPrimeiroPrecoDisponivel - preco diario zerado deve pular para proximo`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 0.0,
            precoSemanal = 200.0,
            precoQuinzenal = 350.0,
            precoMensal = 600.0
        )

        val resultado = equipamento.getPrimeiroPrecoDisponivel()

        assertNotNull(resultado)
        assertEquals(PeriodoLocacao.SEMANAL, resultado.first)
        assertEquals(200.0, resultado.second)
    }

    // ==================== TESTES DE VALORES PADRAO ====================

    @Test
    fun `equipamento com valores padrao deve ter campos corretos`() {
        val equipamento = Equipamento()

        assertEquals("", equipamento.id)
        assertEquals("", equipamento.nome)
        assertEquals("", equipamento.categoria)
        assertNull(equipamento.identificacao)
        assertNull(equipamento.valorCompra)
        assertNull(equipamento.precoDiario)
        assertNull(equipamento.precoSemanal)
        assertNull(equipamento.precoQuinzenal)
        assertNull(equipamento.precoMensal)
        assertNull(equipamento.observacoes)
    }

    @Test
    fun `equipamento COLLECTION_NAME deve ser correto`() {
        assertEquals("equipamentos", Equipamento.COLLECTION_NAME)
    }
}
