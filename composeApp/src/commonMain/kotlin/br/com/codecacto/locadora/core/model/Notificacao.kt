package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Notificacao(
    val id: String = "",
    val titulo: String = "",
    val mensagem: String = "",
    val tipo: String = NotificacaoTipo.INFO.valor,
    val lida: Boolean = false,
    val dados: Map<String, String> = emptyMap(),
    val criadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "notificacoes"
    }
}

enum class NotificacaoTipo(val valor: String) {
    INFO("info"),
    LOCACAO("locacao"),
    ENTREGA("entrega"),
    PAGAMENTO("pagamento"),
    VENCIMENTO("vencimento"),
    SISTEMA("sistema")
}
