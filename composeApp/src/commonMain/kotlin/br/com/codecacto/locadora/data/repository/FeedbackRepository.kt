package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Feedback
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import br.com.codecacto.locadora.currentTimeMillis

interface FeedbackRepository {
    suspend fun sendFeedback(motivo: String, mensagem: String): Result<Unit>
}

class FeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : FeedbackRepository {

    private val collection = firestore.collection(Feedback.COLLECTION_NAME)

    override suspend fun sendFeedback(motivo: String, mensagem: String): Result<Unit> {
        return try {
            val user = authRepository.currentUser
                ?: return Result.failure(Exception("Usuario nao autenticado"))

            val feedback = Feedback(
                usuarioId = user.id,
                usuarioEmail = user.email,
                motivo = motivo,
                mensagem = mensagem,
                criadoEm = currentTimeMillis()
            )

            collection.add(feedback)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
