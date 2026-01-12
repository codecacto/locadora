package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.testutil.FakeEquipamentoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Testes de integração para EquipamentoRepository usando FakeEquipamentoRepository.
 * Estes testes validam o comportamento esperado das operações de CRUD.
 */
class FakeEquipamentoRepositoryTest {

    private val repository = FakeEquipamentoRepository()

    // ==================== TESTES DE getEquipamentos ====================

    @Test
    fun `getEquipamentos com repositorio vazio deve retornar lista vazia`() = runTest {
        repository.clear()

        val equipamentos = repository.getEquipamentos().first()

        assertTrue(equipamentos.isEmpty())
    }

    @Test
    fun `getEquipamentos deve retornar todos os equipamentos cadastrados`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "2", nome = "Andaime 1.5m", categoria = "Andaime")
        )

        val equipamentos = repository.getEquipamentos().first()

        assertEquals(2, equipamentos.size)
    }

    // ==================== TESTES DE getEquipamentoById ====================

    @Test
    fun `getEquipamentoById com id existente deve retornar equipamento`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(
                id = "equip-123",
                nome = "Betoneira 120L",
                categoria = "Betoneira",
                precoDiario = 100.0
            )
        )

        val equipamento = repository.getEquipamentoById("equip-123")

        assertNotNull(equipamento)
        assertEquals("equip-123", equipamento.id)
        assertEquals("Betoneira 120L", equipamento.nome)
        assertEquals(100.0, equipamento.precoDiario)
    }

    @Test
    fun `getEquipamentoById com id inexistente deve retornar null`() = runTest {
        repository.clear()

        val equipamento = repository.getEquipamentoById("inexistente")

        assertNull(equipamento)
    }

    // ==================== TESTES DE addEquipamento ====================

    @Test
    fun `addEquipamento deve adicionar equipamento e retornar id`() = runTest {
        repository.clear()
        val novoEquipamento = Equipamento(
            nome = "Betoneira 400L",
            categoria = "Betoneira",
            precoDiario = 150.0,
            precoSemanal = 500.0,
            precoMensal = 1500.0
        )

        val id = repository.addEquipamento(novoEquipamento)

        assertNotNull(id)
        assertTrue(id.startsWith("equip-"))

        val equipamentoSalvo = repository.getEquipamentoById(id)
        assertNotNull(equipamentoSalvo)
        assertEquals("Betoneira 400L", equipamentoSalvo.nome)
        assertEquals(150.0, equipamentoSalvo.precoDiario)
    }

    @Test
    fun `addEquipamento com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnAdd = true
        repository.exceptionToThrow = Exception("Usuário não autenticado")

        val equipamento = Equipamento(nome = "Teste", categoria = "Teste")

        assertFailsWith<Exception> {
            repository.addEquipamento(equipamento)
        }

        repository.shouldThrowOnAdd = false
    }

    // ==================== TESTES DE updateEquipamento ====================

    @Test
    fun `updateEquipamento deve atualizar dados do equipamento`() = runTest {
        repository.clear()
        val id = repository.addEquipamento(
            Equipamento(
                nome = "Betoneira Original",
                categoria = "Betoneira",
                precoDiario = 100.0
            )
        )

        val equipamentoExistente = repository.getEquipamentoById(id)!!
        repository.updateEquipamento(
            equipamentoExistente.copy(
                nome = "Betoneira Atualizada",
                precoDiario = 120.0,
                precoSemanal = 400.0
            )
        )

        val equipamentoAtualizado = repository.getEquipamentoById(id)
        assertNotNull(equipamentoAtualizado)
        assertEquals("Betoneira Atualizada", equipamentoAtualizado.nome)
        assertEquals(120.0, equipamentoAtualizado.precoDiario)
        assertEquals(400.0, equipamentoAtualizado.precoSemanal)
    }

    @Test
    fun `updateEquipamento com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnUpdate = true

        val equipamento = Equipamento(id = "1", nome = "Teste", categoria = "Teste")

        assertFailsWith<Exception> {
            repository.updateEquipamento(equipamento)
        }

        repository.shouldThrowOnUpdate = false
    }

    // ==================== TESTES DE deleteEquipamento ====================

    @Test
    fun `deleteEquipamento deve remover equipamento do repositorio`() = runTest {
        repository.clear()
        val id = repository.addEquipamento(
            Equipamento(nome = "Betoneira", categoria = "Betoneira")
        )

        repository.deleteEquipamento(id)

        val equipamentoDeletado = repository.getEquipamentoById(id)
        assertNull(equipamentoDeletado)
    }

    @Test
    fun `deleteEquipamento com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnDelete = true

        assertFailsWith<Exception> {
            repository.deleteEquipamento("qualquer-id")
        }

        repository.shouldThrowOnDelete = false
    }

    // ==================== TESTES DE searchEquipamentos ====================

    @Test
    fun `searchEquipamentos por nome deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "2", nome = "Betoneira 400L", categoria = "Betoneira")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "3", nome = "Andaime 1.5m", categoria = "Andaime")
        )

        val resultados = repository.searchEquipamentos("Betoneira")

        assertEquals(2, resultados.size)
        assertTrue(resultados.all { it.nome.contains("Betoneira") })
    }

    @Test
    fun `searchEquipamentos por categoria deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira 120L", categoria = "Betoneira")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "2", nome = "Andaime Tubular", categoria = "Andaime")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "3", nome = "Andaime Fachadeiro", categoria = "Andaime")
        )

        val resultados = repository.searchEquipamentos("Andaime")

        assertEquals(2, resultados.size)
    }

    @Test
    fun `searchEquipamentos por identificacao deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira", identificacao = "BET-001")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "2", nome = "Andaime", categoria = "Andaime", identificacao = "AND-001")
        )

        val resultados = repository.searchEquipamentos("BET-001")

        assertEquals(1, resultados.size)
        assertEquals("BET-001", resultados.first().identificacao)
    }

    @Test
    fun `searchEquipamentos case insensitive deve funcionar`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "BETONEIRA 120L", categoria = "Betoneira")
        )

        val resultados = repository.searchEquipamentos("betoneira")

        assertEquals(1, resultados.size)
    }

    @Test
    fun `searchEquipamentos sem resultados deve retornar lista vazia`() = runTest {
        repository.clear()
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira")
        )

        val resultados = repository.searchEquipamentos("XYZ123")

        assertTrue(resultados.isEmpty())
    }

    // ==================== TESTES DE Helper Methods ====================

    @Test
    fun `setEquipamentos deve substituir todos os equipamentos`() = runTest {
        repository.addEquipamentoDirectly(
            Equipamento(id = "old-1", nome = "Antigo", categoria = "Teste")
        )

        repository.setEquipamentos(listOf(
            Equipamento(id = "new-1", nome = "Novo 1", categoria = "Teste"),
            Equipamento(id = "new-2", nome = "Novo 2", categoria = "Teste")
        ))

        val equipamentos = repository.getEquipamentos().first()
        assertEquals(2, equipamentos.size)
        assertNull(repository.getEquipamentoById("old-1"))
        assertNotNull(repository.getEquipamentoById("new-1"))
    }

    @Test
    fun `clear deve remover todos os equipamentos`() = runTest {
        repository.addEquipamentoDirectly(
            Equipamento(id = "1", nome = "Betoneira", categoria = "Betoneira")
        )
        repository.addEquipamentoDirectly(
            Equipamento(id = "2", nome = "Andaime", categoria = "Andaime")
        )

        repository.clear()

        val equipamentos = repository.getEquipamentos().first()
        assertTrue(equipamentos.isEmpty())
    }

    // ==================== TESTES DE Equipamento com Preços ====================

    @Test
    fun `equipamento com todos os precos deve ser salvo corretamente`() = runTest {
        repository.clear()
        val equipamento = Equipamento(
            nome = "Betoneira Completa",
            categoria = "Betoneira",
            precoDiario = 100.0,
            precoSemanal = 500.0,
            precoQuinzenal = 800.0,
            precoMensal = 1500.0,
            valorCompra = 5000.0
        )

        val id = repository.addEquipamento(equipamento)
        val salvo = repository.getEquipamentoById(id)

        assertNotNull(salvo)
        assertEquals(100.0, salvo.precoDiario)
        assertEquals(500.0, salvo.precoSemanal)
        assertEquals(800.0, salvo.precoQuinzenal)
        assertEquals(1500.0, salvo.precoMensal)
        assertEquals(5000.0, salvo.valorCompra)
    }

    @Test
    fun `equipamento com precos parciais deve ser salvo corretamente`() = runTest {
        repository.clear()
        val equipamento = Equipamento(
            nome = "Betoneira Parcial",
            categoria = "Betoneira",
            precoDiario = 100.0,
            precoMensal = 1500.0
            // precoSemanal e precoQuinzenal são null
        )

        val id = repository.addEquipamento(equipamento)
        val salvo = repository.getEquipamentoById(id)

        assertNotNull(salvo)
        assertEquals(100.0, salvo.precoDiario)
        assertNull(salvo.precoSemanal)
        assertNull(salvo.precoQuinzenal)
        assertEquals(1500.0, salvo.precoMensal)
    }
}
