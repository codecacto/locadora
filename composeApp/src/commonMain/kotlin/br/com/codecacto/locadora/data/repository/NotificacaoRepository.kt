package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf

interface NotificacaoRepository {
    fun getNotificacoes(): Flow<List<Notificacao>>
    fun getUnreadCount(): Flow<Int>
    suspend fun marcarComoLida(id: String): Result<Unit>
    suspend fun marcarTodasComoLidas(): Result<Unit>
    suspend fun deleteNotificacao(id: String): Result<Unit>
    suspend fun deleteTodasLidas(): Result<Unit>
}

class NotificacaoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : NotificacaoRepository {

    private val collection = firestore.collection(Notificacao.COLLECTION_NAME)

    override fun getNotificacoes(): Flow<List<Notificacao>> {
        val userId = authRepository.currentUser?.id ?: return flowOf(emptyList())

        return collection
            .where { "usuarioId" equalTo userId }
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Notificacao>().copy(id = doc.id)
                }
            }
    }

    override fun getUnreadCount(): Flow<Int> {
        val userId = authRepository.currentUser?.id ?: return flowOf(0)

        return collection
            .where { "usuarioId" equalTo userId }
            .where { "lida" equalTo false }
            .snapshots
            .map { snapshot -> snapshot.documents.size }
    }

    override suspend fun marcarComoLida(id: String): Result<Unit> {
        return try {
            collection.document(id).update("lida" to true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarTodasComoLidas(): Result<Unit> {
        return try {
            val userId = authRepository.currentUser?.id
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val snapshot = collection
                .where { "usuarioId" equalTo userId }
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
            collection.document(id).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTodasLidas(): Result<Unit> {
        return try {
            val userId = authRepository.currentUser?.id
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val snapshot = collection
                .where { "usuarioId" equalTo userId }
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
