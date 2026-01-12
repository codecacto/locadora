package br.com.codecacto.locadora.features.equipamentos.presentation

import br.com.codecacto.locadora.core.model.Equipamento
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para EquipamentosContract - State e sua lógica.
 */
class EquipamentosContractTest {

    // ==================== TESTES DE EquipamentoComStatus ====================

    @Test
    fun `EquipamentoComStatus deve manter equipamento e status`() {
        val equipamento = Equipamento(
            id = "1",
            nome = "Betoneira",
            categoria = "Betoneira",
            precoDiario = 100.0
        )

        val comStatus = EquipamentoComStatus(equipamento, isAlugado = true)

        assertEquals(equipamento, comStatus.equipamento)
        assertTrue(comStatus.isAlugado)
    }

    @Test
    fun `EquipamentoComStatus disponivel deve ter isAlugado false`() {
        val equipamento = Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira")
        val comStatus = EquipamentoComStatus(equipamento, isAlugado = false)

        assertFalse(comStatus.isAlugado)
    }

    // ==================== TESTES DE STATE - Valores Padrao ====================

    @Test
    fun `State inicial deve ter valores padrao corretos`() {
        val state = EquipamentosContract.State()

        assertTrue(state.isLoading)
        assertFalse(state.isRefreshing)
        assertFalse(state.isSaving)
        assertTrue(state.equipamentos.isEmpty())
        assertEquals("", state.searchQuery)
        assertFalse(state.showForm)
        assertNull(state.editingEquipamento)
        assertEquals("", state.nome)
        assertEquals("", state.categoria)
        assertEquals("", state.identificacao)
        assertEquals("", state.precoDiario)
        assertEquals("", state.precoSemanal)
        assertEquals("", state.precoQuinzenal)
        assertEquals("", state.precoMensal)
        assertEquals("", state.valorCompra)
        assertEquals("", state.observacoes)
        assertNull(state.error)
    }

    // ==================== TESTES DE filteredEquipamentos ====================

    @Test
    fun `filteredEquipamentos sem busca deve retornar todos os equipamentos`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "2", nome = "Andaime 1.5m", categoria = "Andaime"),
                isAlugado = true
            ),
            EquipamentoComStatus(
                Equipamento(id = "3", nome = "Compactador", categoria = "Compactador"),
                isAlugado = false
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = ""
        )

        assertEquals(3, state.filteredEquipamentos.size)
        assertEquals(equipamentos, state.filteredEquipamentos)
    }

    @Test
    fun `filteredEquipamentos com busca por nome deve filtrar corretamente`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "2", nome = "Andaime 1.5m", categoria = "Andaime"),
                isAlugado = true
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = "Betoneira"
        )

        assertEquals(1, state.filteredEquipamentos.size)
        assertEquals("Betoneira 120L", state.filteredEquipamentos.first().equipamento.nome)
    }

    @Test
    fun `filteredEquipamentos com busca case insensitive deve funcionar`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "BETONEIRA 120L", categoria = "Betoneira"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "2", nome = "Andaime 1.5m", categoria = "Andaime"),
                isAlugado = true
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = "betoneira"
        )

        assertEquals(1, state.filteredEquipamentos.size)
    }

    @Test
    fun `filteredEquipamentos com busca por categoria deve filtrar corretamente`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "2", nome = "Betoneira 400L", categoria = "Betoneira"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "3", nome = "Andaime", categoria = "Andaime"),
                isAlugado = true
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = "Andaime"
        )

        assertEquals(1, state.filteredEquipamentos.size)
        assertEquals("Andaime", state.filteredEquipamentos.first().equipamento.categoria)
    }

    @Test
    fun `filteredEquipamentos com busca por identificacao deve filtrar corretamente`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira", identificacao = "BET-001"),
                isAlugado = false
            ),
            EquipamentoComStatus(
                Equipamento(id = "2", nome = "Andaime", categoria = "Andaime", identificacao = "AND-002"),
                isAlugado = true
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = "BET-001"
        )

        assertEquals(1, state.filteredEquipamentos.size)
        assertEquals("BET-001", state.filteredEquipamentos.first().equipamento.identificacao)
    }

    @Test
    fun `filteredEquipamentos sem resultados deve retornar lista vazia`() {
        val equipamentos = listOf(
            EquipamentoComStatus(
                Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira"),
                isAlugado = false
            )
        )

        val state = EquipamentosContract.State(
            equipamentos = equipamentos,
            searchQuery = "XYZ123"
        )

        assertTrue(state.filteredEquipamentos.isEmpty())
    }

    @Test
    fun `filteredEquipamentos com lista vazia deve retornar lista vazia`() {
        val state = EquipamentosContract.State(
            equipamentos = emptyList(),
            searchQuery = "teste"
        )

        assertTrue(state.filteredEquipamentos.isEmpty())
    }

    // ==================== TESTES DE STATE - Copy ====================

    @Test
    fun `State copy deve atualizar apenas campos especificados`() {
        val initial = EquipamentosContract.State()

        val updated = initial.copy(
            isLoading = false,
            showForm = true,
            nome = "Betoneira",
            categoria = "Betoneira"
        )

        assertFalse(updated.isLoading)
        assertTrue(updated.showForm)
        assertEquals("Betoneira", updated.nome)
        assertEquals("Betoneira", updated.categoria)
        // Campos não alterados devem manter valores originais
        assertEquals(initial.equipamentos, updated.equipamentos)
        assertEquals(initial.precoDiario, updated.precoDiario)
    }

    @Test
    fun `State em modo edicao deve ter editingEquipamento preenchido`() {
        val equipamento = Equipamento(
            id = "equip-123",
            nome = "Betoneira 120L",
            categoria = "Betoneira",
            precoDiario = 100.0
        )

        val state = EquipamentosContract.State(
            showForm = true,
            editingEquipamento = equipamento,
            nome = equipamento.nome,
            categoria = equipamento.categoria
        )

        assertTrue(state.showForm)
        assertNotNull(state.editingEquipamento)
        assertEquals("equip-123", state.editingEquipamento?.id)
    }

    @Test
    fun `State com precos preenchidos deve manter valores`() {
        val state = EquipamentosContract.State(
            precoDiario = "10000",  // R$ 100,00 em centavos
            precoSemanal = "50000",
            precoQuinzenal = "80000",
            precoMensal = "150000"
        )

        assertEquals("10000", state.precoDiario)
        assertEquals("50000", state.precoSemanal)
        assertEquals("80000", state.precoQuinzenal)
        assertEquals("150000", state.precoMensal)
    }

    // ==================== TESTES DE Actions ====================

    @Test
    fun `Action Search deve conter query correta`() {
        val action = EquipamentosContract.Action.Search("betoneira")
        assertEquals("betoneira", action.query)
    }

    @Test
    fun `Action SetNome deve conter valor correto`() {
        val action = EquipamentosContract.Action.SetNome("Betoneira 120L")
        assertEquals("Betoneira 120L", action.value)
    }

    @Test
    fun `Action SetCategoria deve conter valor correto`() {
        val action = EquipamentosContract.Action.SetCategoria("Betoneira")
        assertEquals("Betoneira", action.value)
    }

    @Test
    fun `Action SetPrecoDiario deve conter valor correto`() {
        val action = EquipamentosContract.Action.SetPrecoDiario("10000")
        assertEquals("10000", action.value)
    }

    @Test
    fun `Action EditEquipamento deve conter equipamento correto`() {
        val equipamento = Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira")
        val action = EquipamentosContract.Action.EditEquipamento(equipamento)

        assertEquals(equipamento, action.equipamento)
    }

    @Test
    fun `Action DeleteEquipamento deve conter equipamento correto`() {
        val equipamento = Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira")
        val action = EquipamentosContract.Action.DeleteEquipamento(equipamento)

        assertEquals(equipamento, action.equipamento)
    }

    // ==================== TESTES DE Effects ====================

    @Test
    fun `Effect ShowSuccess deve conter mensagem correta`() {
        val effect = EquipamentosContract.Effect.ShowSuccess("Equipamento salvo com sucesso!")
        assertEquals("Equipamento salvo com sucesso!", effect.message)
    }

    @Test
    fun `Effect ShowError deve conter mensagem correta`() {
        val effect = EquipamentosContract.Effect.ShowError("Erro ao salvar equipamento")
        assertEquals("Erro ao salvar equipamento", effect.message)
    }
}
