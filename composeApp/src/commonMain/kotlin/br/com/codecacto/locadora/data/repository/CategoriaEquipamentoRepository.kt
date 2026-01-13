package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.CategoriaEquipamento
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface CategoriaEquipamentoRepository {
    fun getCategorias(): Flow<List<CategoriaEquipamento>>
    suspend fun initializeDefaultCategorias()
}

class CategoriaEquipamentoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : CategoriaEquipamentoRepository {

    private val collection get() = firestore.collection(CategoriaEquipamento.COLLECTION_NAME)

    override fun getCategorias(): Flow<List<CategoriaEquipamento>> {
        return collection
            .orderBy("ordem", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<CategoriaEquipamento>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun initializeDefaultCategorias() {
        val snapshot = collection.get()
        if (snapshot.documents.isNotEmpty()) return

        val defaultCategorias = listOf(
            CategoriaEquipamento(nome = "Andaime", ordem = 1),
            CategoriaEquipamento(nome = "Betoneira", ordem = 2),
            CategoriaEquipamento(nome = "Compactador", ordem = 3),
            CategoriaEquipamento(nome = "Compressor", ordem = 4),
            CategoriaEquipamento(nome = "Escora", ordem = 5),
            CategoriaEquipamento(nome = "Furadeira", ordem = 6),
            CategoriaEquipamento(nome = "Gerador", ordem = 7),
            CategoriaEquipamento(nome = "Guincho", ordem = 8),
            CategoriaEquipamento(nome = "Martelete", ordem = 9),
            CategoriaEquipamento(nome = "Misturador de Argamassa", ordem = 10),
            CategoriaEquipamento(nome = "Plataforma", ordem = 11),
            CategoriaEquipamento(nome = "Riscadora de Porcelanato", ordem = 12),
            CategoriaEquipamento(nome = "Rompedor", ordem = 13),
            CategoriaEquipamento(nome = "Serra", ordem = 14),
            CategoriaEquipamento(nome = "Soldadora", ordem = 15),
            CategoriaEquipamento(nome = "Vibrador", ordem = 16),
            CategoriaEquipamento(nome = "Outros", ordem = 99)
        )

        defaultCategorias.forEach { categoria ->
            collection.add(categoria)
        }
    }
}
