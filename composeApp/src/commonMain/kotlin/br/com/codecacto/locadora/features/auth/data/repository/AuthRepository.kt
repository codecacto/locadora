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
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun changeEmail(newEmail: String, password: String): Result<Unit>
    suspend fun updateProfile(displayName: String): Result<Unit>
    suspend fun deleteAccount(password: String): Result<Unit>
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

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Usuario nao autenticado")
            val email = user.email ?: throw Exception("Email nao encontrado")

            // Reautenticar usuario
            firebaseAuth.signInWithEmailAndPassword(email, currentPassword)

            // Alterar senha
            user.updatePassword(newPassword)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun changeEmail(newEmail: String, password: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Usuario nao autenticado")
            val currentEmail = user.email ?: throw Exception("Email nao encontrado")

            // Reautenticar usuario
            firebaseAuth.signInWithEmailAndPassword(currentEmail, password)

            // Alterar email
            user.updateEmail(newEmail)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun updateProfile(displayName: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Usuario nao autenticado")
            user.updateProfile(displayName = displayName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: throw Exception("Usuario nao autenticado")
            val email = user.email ?: throw Exception("Email nao encontrado")

            // Reautenticar usuario antes de deletar
            firebaseAuth.signInWithEmailAndPassword(email, password)

            // Deletar conta
            user.delete()

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
            // Credenciais inválidas
            message.contains("invalid-credential") ||
            message.contains("wrong-password") ||
            message.contains("invalid_login_credentials") ||
            message.contains("invalid-login-credentials") ||
            message.contains("user-not-found") ||
            message.contains("invalid password") ||
            message.contains("password is invalid") ||
            message.contains("no user record") ->
                Exception("Email ou senha incorretos")

            // Email já cadastrado
            message.contains("email-already-in-use") ||
            message.contains("email already in use") ->
                Exception("Este email ja esta cadastrado")

            // Senha fraca
            message.contains("weak-password") ||
            message.contains("weak password") ||
            message.contains("password should be") ->
                Exception("A senha deve ter pelo menos 6 caracteres")

            // Email inválido
            message.contains("invalid-email") ||
            message.contains("invalid email") ||
            message.contains("badly formatted") ->
                Exception("Email invalido")

            // Muitas tentativas
            message.contains("too-many-requests") ||
            message.contains("too many requests") ||
            message.contains("blocked") ->
                Exception("Muitas tentativas. Aguarde alguns minutos e tente novamente")

            // Erro de rede
            message.contains("network") ||
            message.contains("internet") ||
            message.contains("connection") ||
            message.contains("timeout") ->
                Exception("Erro de conexao. Verifique sua internet")

            // Usuário desabilitado
            message.contains("user-disabled") ||
            message.contains("user disabled") ->
                Exception("Esta conta foi desativada")

            // Conta não encontrada
            message.contains("account-exists") ->
                Exception("Ja existe uma conta com este email")

            // Reautenticação necessária
            message.contains("requires-recent-login") ||
            message.contains("recent login") ->
                Exception("Por seguranca, faca login novamente para continuar")

            // Erro genérico - retorna mensagem amigável
            else -> Exception("Ocorreu um erro. Tente novamente")
        }
    }
}
