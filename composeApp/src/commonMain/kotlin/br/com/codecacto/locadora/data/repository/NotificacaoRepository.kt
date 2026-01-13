package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import br.com.codecacto.locadora.currentTimeMillis

interface NotificacaoRepository {
    fun getNotificacoes(): Flow<List<Notificacao>>
    fun getUnreadCount(): Flow<Int>
    suspend fun criarNotificacao(notificacao: Notificacao): Result<String>
    suspend fun marcarComoLida(id: String): Result<Unit>
    suspend fun marcarTodasComoLidas(): Result<Unit>
    suspend fun deleteNotificacao(id: String): Result<Unit>
    suspend fun deleteTodasLidas(): Result<Unit>
}

class NotificacaoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : NotificacaoRepository {

    private fun getUserCollection() = authRepository.currentUser?.id?.let { userId ->
        firestore.collection("usuarios").document(userId).collection(Notificacao.COLLECTION_NAME)
    }

    override fun getNotificacoes(): Flow<List<Notificacao>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Notificacao>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override fun getUnreadCount(): Flow<Int> {
        val collection = getUserCollection() ?: return flowOf(0)

        return collection
            .where { "lida" equalTo false }
            .snapshots
            .map { snapshot -> snapshot.documents.size }
            .catch { emit(0) }
    }

    override suspend fun criarNotificacao(notificacao: Notificacao): Result<String> {
        return try {
            val collection = getUserCollection()
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val docRef = collection.add(notificacao.copy(
                criadoEm = currentTimeMillis()
            ))
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarComoLida(id: String): Result<Unit> {
        return try {
            val collection = getUserCollection()
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            collection.document(id).update("lida" to true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarTodasComoLidas(): Result<Unit> {
        return try {
            val collection = getUserCollection()
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val snapshot = collection
                .where { "lida" equalTo false }
                .get()

            snapshot.documents.forEach { doc ->
                collection.document(doc.id).update("lida" to true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotificacao(id: String): Result<Unit> {
        return try {
            val collection = getUserCollection()
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            collection.document(id).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTodasLidas(): Result<Unit> {
        return try {
            val collection = getUserCollection()
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val snapshot = collection
                .where { "lida" equalTo true }
                .get()

            snapshot.documents.forEach { doc ->
                collection.document(doc.id).delete()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
