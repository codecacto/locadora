package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoriaEquipamento(
    val id: String = "",
    val nome: String = "",
    val ordem: Int = 0
) {
    companion object {
        const val COLLECTION_NAME = "categorias_equipamentos"
    }
}
