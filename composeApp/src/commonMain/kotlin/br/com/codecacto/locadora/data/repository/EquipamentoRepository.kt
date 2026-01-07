package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Equipamento
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface EquipamentoRepository {
    fun getEquipamentos(): Flow<List<Equipamento>>
    suspend fun getEquipamentoById(id: String): Equipamento?
    suspend fun addEquipamento(equipamento: Equipamento): String
    suspend fun updateEquipamento(equipamento: Equipamento)
    suspend fun deleteEquipamento(id: String)
    suspend fun searchEquipamentos(query: String): List<Equipamento>
}

class EquipamentoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EquipamentoRepository {

    private val collection = firestore.collection(Equipamento.COLLECTION_NAME)

    override fun getEquipamentos(): Flow<List<Equipamento>> {
        return collection
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Equipamento>().copy(id = doc.id)
                }
            }
    }

    override suspend fun getEquipamentoById(id: String): Equipamento? {
        return try {
            val doc = collection.document(id).get()
            if (doc.exists) {
                doc.data<Equipamento>().copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addEquipamento(equipamento: Equipamento): String {
        val docRef = collection.add(equipamento.copy(
            criadoEm = System.currentTimeMillis(),
            atualizadoEm = System.currentTimeMillis()
        ))
        return docRef.id
    }

    override suspend fun updateEquipamento(equipamento: Equipamento) {
        collection.document(equipamento.id).set(
            equipamento.copy(atualizadoEm = System.currentTimeMillis())
        )
    }

    override suspend fun deleteEquipamento(id: String) {
        collection.document(id).delete()
    }

    override suspend fun searchEquipamentos(query: String): List<Equipamento> {
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
