package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import br.com.codecacto.locadora.currentTimeMillis

interface EquipamentoRepository {
    fun getEquipamentos(): Flow<List<Equipamento>>
    suspend fun getEquipamentoById(id: String): Equipamento?
    suspend fun addEquipamento(equipamento: Equipamento): String
    suspend fun updateEquipamento(equipamento: Equipamento)
    suspend fun deleteEquipamento(id: String)
    suspend fun searchEquipamentos(query: String): List<Equipamento>
}

class EquipamentoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : EquipamentoRepository {

    private fun getUserCollection() = authRepository.currentUser?.id?.let { userId ->
        firestore.collection("usuarios").document(userId).collection(Equipamento.COLLECTION_NAME)
    }

    override fun getEquipamentos(): Flow<List<Equipamento>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Equipamento>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun getEquipamentoById(id: String): Equipamento? {
        val collection = getUserCollection() ?: return null
        val doc = collection.document(id).get()
        return if (doc.exists) {
            doc.data<Equipamento>().copy(id = doc.id)
        } else null
    }

    override suspend fun addEquipamento(equipamento: Equipamento): String {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        val docRef = collection.add(equipamento.copy(
            criadoEm = currentTimeMillis(),
            atualizadoEm = currentTimeMillis()
        ))
        return docRef.id
    }

    override suspend fun updateEquipamento(equipamento: Equipamento) {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        collection.document(equipamento.id).set(
            equipamento.copy(atualizadoEm = currentTimeMillis())
        )
    }

    override suspend fun deleteEquipamento(id: String) {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        collection.document(id).delete()
    }

    override suspend fun searchEquipamentos(query: String): List<Equipamento> {
        val collection = getUserCollection() ?: return emptyList()

        val snapshot = collection.get()
        val queryLower = query.lowercase()
        return snapshot.documents
            .map { doc -> doc.data<Equipamento>().copy(id = doc.id) }
            .filter { equipamento ->
                equipamento.nome.lowercase().contains(queryLower) ||
                equipamento.categoria.lowercase().contains(queryLower) ||
                equipamento.identificacao?.lowercase()?.contains(queryLower) == true
            }
    }
}
