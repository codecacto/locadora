package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import br.com.codecacto.locadora.currentTimeMillis

interface RecebimentoRepository {
    fun getRecebimentos(): Flow<List<Recebimento>>
    fun getRecebimentosPendentes(): Flow<List<Recebimento>>
    fun getRecebimentosPagos(): Flow<List<Recebimento>>
    fun getRecebimentosByLocacao(locacaoId: String): Flow<List<Recebimento>>
    suspend fun addRecebimento(recebimento: Recebimento): String
    suspend fun marcarPago(recebimentoId: String)
    suspend fun deleteRecebimento(recebimentoId: String)
    suspend fun deleteRecebimentosByLocacao(locacaoId: String)
}

class RecebimentoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : RecebimentoRepository {

    private fun getUserCollection() = authRepository.currentUser?.id?.let { userId ->
        firestore.collection("usuarios").document(userId).collection(Recebimento.COLLECTION_NAME)
    }

    override fun getRecebimentos(): Flow<List<Recebimento>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .orderBy("dataVencimento", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Recebimento>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override fun getRecebimentosPendentes(): Flow<List<Recebimento>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .where { "status" equalTo StatusPagamento.PENDENTE.name }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Recebimento>().copy(id = doc.id)
                }.sortedBy { it.dataVencimento }
            }
            .catch { emit(emptyList()) }
    }

    override fun getRecebimentosPagos(): Flow<List<Recebimento>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .where { "status" equalTo StatusPagamento.PAGO.name }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Recebimento>().copy(id = doc.id)
                }.sortedByDescending { it.dataPagamento ?: it.criadoEm }
            }
            .catch { emit(emptyList()) }
    }

    override fun getRecebimentosByLocacao(locacaoId: String): Flow<List<Recebimento>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .where { "locacaoId" equalTo locacaoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Recebimento>().copy(id = doc.id)
                }.sortedBy { it.numeroRenovacao }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun addRecebimento(recebimento: Recebimento): String {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        val docRef = collection.add(recebimento.copy(
            criadoEm = currentTimeMillis()
        ))
        return docRef.id
    }

    override suspend fun marcarPago(recebimentoId: String) {
        val collection = getUserCollection() ?: return
        val doc = collection.document(recebimentoId).get()
        if (doc.exists) {
            val recebimento = doc.data<Recebimento>().copy(id = doc.id)
            collection.document(recebimentoId).set(
                recebimento.copy(
                    status = StatusPagamento.PAGO,
                    dataPagamento = currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deleteRecebimento(recebimentoId: String) {
        val collection = getUserCollection() ?: return
        collection.document(recebimentoId).delete()
    }

    override suspend fun deleteRecebimentosByLocacao(locacaoId: String) {
        val collection = getUserCollection() ?: return
        val snapshot = collection
            .where { "locacaoId" equalTo locacaoId }
            .get()

        snapshot.documents.forEach { doc ->
            collection.document(doc.id).delete()
        }
    }
}
