package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface ClienteRepository {
    fun getClientes(): Flow<List<Cliente>>
    suspend fun getClienteById(id: String): Cliente?
    suspend fun addCliente(cliente: Cliente): String
    suspend fun updateCliente(cliente: Cliente)
    suspend fun deleteCliente(id: String)
    suspend fun searchClientes(query: String): List<Cliente>
}

class ClienteRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ClienteRepository {

    private val collection = firestore.collection(Cliente.COLLECTION_NAME)

    override fun getClientes(): Flow<List<Cliente>> {
        val userId = authRepository.currentUser?.id ?: return flowOf(emptyList())

        return collection
            .where { "userId" equalTo userId }
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Cliente>().copy(id = doc.id)
                }
            }
    }

    override suspend fun getClienteById(id: String): Cliente? {
        val doc = collection.document(id).get()
        return if (doc.exists) {
            doc.data<Cliente>().copy(id = doc.id)
        } else null
    }

    override suspend fun addCliente(cliente: Cliente): String {
        val userId = authRepository.currentUser?.id
            ?: throw Exception("Usuario nao autenticado")

        val docRef = collection.add(cliente.copy(
            userId = userId,
            criadoEm = System.currentTimeMillis(),
            atualizadoEm = System.currentTimeMillis()
        ))
        return docRef.id
    }

    override suspend fun updateCliente(cliente: Cliente) {
        collection.document(cliente.id).set(
            cliente.copy(atualizadoEm = System.currentTimeMillis())
        )
    }

    override suspend fun deleteCliente(id: String) {
        collection.document(id).delete()
    }

    override suspend fun searchClientes(query: String): List<Cliente> {
        val userId = authRepository.currentUser?.id ?: return emptyList()

        val snapshot = collection
            .where { "userId" equalTo userId }
            .get()
        val queryLower = query.lowercase()
        return snapshot.documents
            .map { doc -> doc.data<Cliente>().copy(id = doc.id) }
            .filter { cliente ->
                cliente.nomeRazao.lowercase().contains(queryLower) ||
                cliente.cpfCnpj?.lowercase()?.contains(queryLower) == true ||
                cliente.telefoneWhatsapp.contains(query)
            }
    }
}
