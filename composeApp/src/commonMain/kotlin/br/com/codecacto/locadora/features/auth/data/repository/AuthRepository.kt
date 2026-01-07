package br.com.codecacto.locadora.features.auth.data.repository

import br.com.codecacto.locadora.features.auth.domain.model.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AuthRepository {
    val currentUser: User?
    val isAuthenticated: Boolean
    fun observeAuthState(): Flow<User?>

    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun registerWithEmail(email: String, password: String, displayName: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun logout()
}

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : AuthRepository {

    override val currentUser: User?
        get() = firebaseAuth.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoURL,
                isEmailVerified = firebaseUser.isEmailVerified,
                providerId = firebaseUser.providerData.firstOrNull()?.providerId ?: "password"
            )
        }

    override val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    override fun observeAuthState(): Flow<User?> {
        return firebaseAuth.authStateChanged.map { firebaseUser ->
            firebaseUser?.let {
                User(
                    id = it.uid,
                    email = it.email ?: "",
                    displayName = it.displayName,
                    photoUrl = it.photoURL,
                    isEmailVerified = it.isEmailVerified,
                    providerId = it.providerData.firstOrNull()?.providerId ?: "password"
                )
            }
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
            val user = result.user ?: throw Exception("Usuario nao encontrado")
            Result.success(
                User(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName,
                    photoUrl = user.photoURL,
                    isEmailVerified = user.isEmailVerified,
                    providerId = "password"
                )
            )
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
            val user = result.user ?: throw Exception("Erro ao criar usuario")

            // Update display name
            user.updateProfile(displayName = displayName)

            Result.success(
                User(
                    id = user.uid,
                    email = user.email ?: "",
                    displayName = displayName,
                    photoUrl = user.photoURL,
                    isEmailVerified = user.isEmailVerified,
                    providerId = "password"
                )
            )
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    private fun mapFirebaseException(e: Exception): Exception {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("invalid-credential") ||
            message.contains("wrong-password") ||
            message.contains("user-not-found") -> Exception("Email ou senha incorretos")

            message.contains("email-already-in-use") -> Exception("Este email ja esta cadastrado")

            message.contains("weak-password") -> Exception("A senha deve ter pelo menos 6 caracteres")

            message.contains("invalid-email") -> Exception("Email invalido")

            message.contains("too-many-requests") -> Exception("Muitas tentativas. Tente novamente mais tarde")

            message.contains("network") -> Exception("Erro de conexao. Verifique sua internet")

            else -> Exception(e.message ?: "Erro desconhecido")
        }
    }
}
