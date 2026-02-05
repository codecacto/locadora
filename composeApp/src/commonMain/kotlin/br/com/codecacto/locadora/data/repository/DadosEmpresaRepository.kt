package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.DadosEmpresa
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface DadosEmpresaRepository {
    fun getDadosEmpresa(): Flow<DadosEmpresa>
    suspend fun saveDadosEmpresa(dados: DadosEmpresa)
}

class DadosEmpresaRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : DadosEmpresaRepository {

    companion object {
        private const val DOCUMENT_NAME = "dadosEmpresa"
    }

    private fun getUserDocument() = authRepository.currentUser?.id?.let { userId ->
        firestore.collection("usuarios").document(userId).collection("configuracoes").document(DOCUMENT_NAME)
    }

    override fun getDadosEmpresa(): Flow<DadosEmpresa> {
        val document = getUserDocument() ?: return flowOf(DadosEmpresa())

        return document.snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    snapshot.data<DadosEmpresa>()
                } else {
                    DadosEmpresa()
                }
            }
            .catch { emit(DadosEmpresa()) }
    }

    override suspend fun saveDadosEmpresa(dados: DadosEmpresa) {
        val document = getUserDocument()
            ?: throw Exception("Usuário não autenticado")

        document.set(dados)
    }
}
