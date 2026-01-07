package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Cliente(
    val id: String = "",
    val nomeRazao: String = "",
    val cpfCnpj: String? = null,
    val telefoneWhatsapp: String = "",
    val email: String? = null,
    val endereco: String? = null,
    val precisaNotaFiscalPadrao: Boolean = false,
    val criadoEm: Long = System.currentTimeMillis(),
    val atualizadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "clientes"
    }
}
