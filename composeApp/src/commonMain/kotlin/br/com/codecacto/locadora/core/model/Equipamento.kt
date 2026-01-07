package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Equipamento(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val identificacao: String? = null,
    val valorCompra: Double? = null,
    val precoPadraoLocacao: Double = 0.0,
    val observacoes: String? = null,
    val criadoEm: Long = System.currentTimeMillis(),
    val atualizadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "equipamentos"
    }
}
