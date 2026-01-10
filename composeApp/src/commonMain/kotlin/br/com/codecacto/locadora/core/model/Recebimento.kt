package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Recebimento(
    val id: String = "",
    val locacaoId: String = "",
    val clienteId: String = "",
    val equipamentoId: String = "",
    val valor: Double = 0.0,
    val dataVencimento: Long = System.currentTimeMillis(),
    val status: StatusPagamento = StatusPagamento.PENDENTE,
    val dataPagamento: Long? = null,
    val numeroRenovacao: Int = 0, // 0 = locação original, 1+ = renovações
    val criadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "recebimentos"
    }
}
