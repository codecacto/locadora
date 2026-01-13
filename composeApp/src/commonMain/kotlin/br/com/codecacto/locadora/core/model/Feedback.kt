package br.com.codecacto.locadora.core.model

import br.com.codecacto.locadora.currentTimeMillis
import kotlinx.serialization.Serializable

@Serializable
data class Feedback(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioEmail: String = "",
    val motivo: String = "",
    val mensagem: String = "",
    val criadoEm: Long = currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "feedbacks"
    }
}

enum class FeedbackMotivo(val valor: String, val label: String) {
    SUGESTAO("sugestao", "Sugestao"),
    BUG("bug", "Reportar Bug"),
    RECLAMACAO("reclamacao", "Reclamacao"),
    DUVIDA("duvida", "Duvida"),
    ELOGIO("elogio", "Elogio"),
    OUTRO("outro", "Outro")
}
