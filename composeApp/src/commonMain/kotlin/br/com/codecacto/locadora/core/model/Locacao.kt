package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class StatusEntrega {
    NAO_AGENDADA,
    AGENDADA,
    ENTREGUE
}

@Serializable
enum class StatusPagamento {
    PENDENTE,
    PAGO
}

@Serializable
enum class StatusColeta {
    NAO_COLETADO,
    COLETADO
}

@Serializable
enum class StatusLocacao {
    ATIVA,
    FINALIZADA
}

enum class StatusPrazo {
    NORMAL,
    PROXIMO_VENCIMENTO,
    VENCIDO
}

@Serializable
data class Locacao(
    val id: String = "",
    val clienteId: String = "",
    val equipamentoId: String = "",
    val valorLocacao: Double = 0.0,
    val dataInicio: Long = System.currentTimeMillis(),
    val dataFimPrevista: Long = System.currentTimeMillis(),
    val statusEntrega: StatusEntrega = StatusEntrega.NAO_AGENDADA,
    val dataEntregaPrevista: Long? = null,
    val dataEntregaReal: Long? = null,
    val statusPagamento: StatusPagamento = StatusPagamento.PENDENTE,
    val dataPagamento: Long? = null,
    val statusColeta: StatusColeta = StatusColeta.NAO_COLETADO,
    val dataColeta: Long? = null,
    val emitirNota: Boolean = false,
    val notaEmitida: Boolean = false,
    val statusLocacao: StatusLocacao = StatusLocacao.ATIVA,
    val qtdRenovacoes: Int = 0,
    val ultimaRenovacaoEm: Long? = null,
    val criadoEm: Long = System.currentTimeMillis(),
    val atualizadoEm: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "locacoes"
        const val DIAS_ALERTA_VENCIMENTO = 2
    }
}
