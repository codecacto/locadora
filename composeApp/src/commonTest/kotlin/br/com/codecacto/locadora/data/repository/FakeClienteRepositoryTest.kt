package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.testutil.FakeClienteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

/**
 * Testes de integração para ClienteRepository usando FakeClienteRepository.
 * Estes testes validam o comportamento esperado das operações de CRUD.
 */
class FakeClienteRepositoryTest {

    private val repository = FakeClienteRepository()

    // ==================== TESTES DE getClientes ====================

    @Test
    fun `getClientes com repositorio vazio deve retornar lista vazia`() = runTest {
        repository.clear()

        val clientes = repository.getClientes().first()

        assertTrue(clientes.isEmpty())
    }

    @Test
    fun `getClientes deve retornar todos os clientes cadastrados`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )
        repository.addClienteDirectly(
            Cliente(id = "2", nomeRazao = "Maria", telefoneWhatsapp = "(11) 88888-7777")
        )

        val clientes = repository.getClientes().first()

        assertEquals(2, clientes.size)
    }

    // ==================== TESTES DE getClienteById ====================

    @Test
    fun `getClienteById com id existente deve retornar cliente`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "cli-123", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )

        val cliente = repository.getClienteById("cli-123")

        assertNotNull(cliente)
        assertEquals("cli-123", cliente.id)
        assertEquals("João", cliente.nomeRazao)
    }

    @Test
    fun `getClienteById com id inexistente deve retornar null`() = runTest {
        repository.clear()

        val cliente = repository.getClienteById("inexistente")

        assertNull(cliente)
    }

    // ==================== TESTES DE addCliente ====================

    @Test
    fun `addCliente deve adicionar cliente e retornar id`() = runTest {
        repository.clear()
        val novoCliente = Cliente(
            nomeRazao = "João Silva",
            telefoneWhatsapp = "(11) 99999-8888",
            email = "joao@email.com"
        )

        val id = repository.addCliente(novoCliente)

        assertNotNull(id)
        assertTrue(id.startsWith("cli-"))

        val clienteSalvo = repository.getClienteById(id)
        assertNotNull(clienteSalvo)
        assertEquals("João Silva", clienteSalvo.nomeRazao)
    }

    @Test
    fun `addCliente com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnAdd = true
        repository.exceptionToThrow = Exception("Usuário não autenticado")

        val cliente = Cliente(nomeRazao = "Teste", telefoneWhatsapp = "(11) 99999-8888")

        assertFailsWith<Exception> {
            repository.addCliente(cliente)
        }

        repository.shouldThrowOnAdd = false
    }

    // ==================== TESTES DE updateCliente ====================

    @Test
    fun `updateCliente deve atualizar dados do cliente`() = runTest {
        repository.clear()
        val id = repository.addCliente(
            Cliente(nomeRazao = "João Original", telefoneWhatsapp = "(11) 99999-8888")
        )

        val clienteExistente = repository.getClienteById(id)!!
        repository.updateCliente(
            clienteExistente.copy(nomeRazao = "João Atualizado", email = "novo@email.com")
        )

        val clienteAtualizado = repository.getClienteById(id)
        assertNotNull(clienteAtualizado)
        assertEquals("João Atualizado", clienteAtualizado.nomeRazao)
        assertEquals("novo@email.com", clienteAtualizado.email)
    }

    @Test
    fun `updateCliente com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnUpdate = true

        val cliente = Cliente(id = "1", nomeRazao = "Teste", telefoneWhatsapp = "(11) 99999-8888")

        assertFailsWith<Exception> {
            repository.updateCliente(cliente)
        }

        repository.shouldThrowOnUpdate = false
    }

    // ==================== TESTES DE deleteCliente ====================

    @Test
    fun `deleteCliente deve remover cliente do repositorio`() = runTest {
        repository.clear()
        val id = repository.addCliente(
            Cliente(nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )

        repository.deleteCliente(id)

        val clienteDeletado = repository.getClienteById(id)
        assertNull(clienteDeletado)
    }

    @Test
    fun `deleteCliente com erro deve lancar excecao`() = runTest {
        repository.clear()
        repository.shouldThrowOnDelete = true

        assertFailsWith<Exception> {
            repository.deleteCliente("qualquer-id")
        }

        repository.shouldThrowOnDelete = false
    }

    // ==================== TESTES DE searchClientes ====================

    @Test
    fun `searchClientes por nome deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João Silva", telefoneWhatsapp = "(11) 99999-8888")
        )
        repository.addClienteDirectly(
            Cliente(id = "2", nomeRazao = "Maria Santos", telefoneWhatsapp = "(11) 88888-7777")
        )
        repository.addClienteDirectly(
            Cliente(id = "3", nomeRazao = "João Pedro", telefoneWhatsapp = "(11) 77777-6666")
        )

        val resultados = repository.searchClientes("João")

        assertEquals(2, resultados.size)
        assertTrue(resultados.all { it.nomeRazao.contains("João") })
    }

    @Test
    fun `searchClientes por CPF deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João", cpfCnpj = "12345678909", telefoneWhatsapp = "(11) 99999-8888")
        )
        repository.addClienteDirectly(
            Cliente(id = "2", nomeRazao = "Maria", cpfCnpj = "98765432100", telefoneWhatsapp = "(11) 88888-7777")
        )

        val resultados = repository.searchClientes("123456")

        assertEquals(1, resultados.size)
        assertEquals("12345678909", resultados.first().cpfCnpj)
    }

    @Test
    fun `searchClientes por telefone deve retornar resultados corretos`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )
        repository.addClienteDirectly(
            Cliente(id = "2", nomeRazao = "Maria", telefoneWhatsapp = "(11) 88888-7777")
        )

        val resultados = repository.searchClientes("99999")

        assertEquals(1, resultados.size)
    }

    @Test
    fun `searchClientes sem resultados deve retornar lista vazia`() = runTest {
        repository.clear()
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )

        val resultados = repository.searchClientes("XYZ123")

        assertTrue(resultados.isEmpty())
    }

    // ==================== TESTES DE Helper Methods ====================

    @Test
    fun `setClientes deve substituir todos os clientes`() = runTest {
        repository.addClienteDirectly(
            Cliente(id = "old-1", nomeRazao = "Antigo", telefoneWhatsapp = "(11) 11111-1111")
        )

        repository.setClientes(listOf(
            Cliente(id = "new-1", nomeRazao = "Novo 1", telefoneWhatsapp = "(11) 22222-2222"),
            Cliente(id = "new-2", nomeRazao = "Novo 2", telefoneWhatsapp = "(11) 33333-3333")
        ))

        val clientes = repository.getClientes().first()
        assertEquals(2, clientes.size)
        assertNull(repository.getClienteById("old-1"))
        assertNotNull(repository.getClienteById("new-1"))
    }

    @Test
    fun `clear deve remover todos os clientes`() = runTest {
        repository.addClienteDirectly(
            Cliente(id = "1", nomeRazao = "João", telefoneWhatsapp = "(11) 99999-8888")
        )
        repository.addClienteDirectly(
            Cliente(id = "2", nomeRazao = "Maria", telefoneWhatsapp = "(11) 88888-7777")
        )

        repository.clear()

        val clientes = repository.getClientes().first()
        assertTrue(clientes.isEmpty())
    }
}
