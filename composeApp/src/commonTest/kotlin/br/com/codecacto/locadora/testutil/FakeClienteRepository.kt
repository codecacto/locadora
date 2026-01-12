package br.com.codecacto.locadora.testutil

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.data.repository.ClienteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake ClienteRepository para testes.
 */
class FakeClienteRepository : ClienteRepository {

    private val clientesFlow = MutableStateFlow<List<Cliente>>(emptyList())
    private var nextId = 1

    var shouldThrowOnAdd = false
    var shouldThrowOnUpdate = false
    var shouldThrowOnDelete = false
    var exceptionToThrow: Exception = Exception("Erro simulado")

    override fun getClientes(): Flow<List<Cliente>> = clientesFlow

    override suspend fun getClienteById(id: String): Cliente? {
        return clientesFlow.value.find { it.id == id }
    }

    override suspend fun addCliente(cliente: Cliente): String {
        if (shouldThrowOnAdd) throw exceptionToThrow

        val id = "cli-${nextId++}"
        val newCliente = cliente.copy(id = id)
        clientesFlow.value = clientesFlow.value + newCliente
        return id
    }

    override suspend fun updateCliente(cliente: Cliente) {
        if (shouldThrowOnUpdate) throw exceptionToThrow

        clientesFlow.value = clientesFlow.value.map {
            if (it.id == cliente.id) cliente else it
        }
    }

    override suspend fun deleteCliente(id: String) {
        if (shouldThrowOnDelete) throw exceptionToThrow

        clientesFlow.value = clientesFlow.value.filter { it.id != id }
    }

    override suspend fun searchClientes(query: String): List<Cliente> {
        val queryLower = query.lowercase()
        return clientesFlow.value.filter { cliente ->
            cliente.nomeRazao.lowercase().contains(queryLower) ||
            cliente.cpfCnpj?.lowercase()?.contains(queryLower) == true ||
            cliente.telefoneWhatsapp.contains(query)
        }
    }

    // Helpers para testes
    fun setClientes(clientes: List<Cliente>) {
        clientesFlow.value = clientes
    }

    fun addClienteDirectly(cliente: Cliente) {
        clientesFlow.value = clientesFlow.value + cliente
    }

    fun clear() {
        clientesFlow.value = emptyList()
        nextId = 1
    }
}
