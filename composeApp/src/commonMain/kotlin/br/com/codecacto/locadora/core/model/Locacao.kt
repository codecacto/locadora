package br.com.codecacto.locadora.core.model

import br.com.codecacto.locadora.currentTimeMillis
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
    // Campo antigo mantido para compatibilidade com dados existentes (v1)
    val equipamentoId: String = "",
    // Campo para múltiplos equipamentos (v2)
    val equipamentoIds: List<String> = emptyList(),
    // Novo campo com itens detalhados - quantidade e patrimônios (v3)
    val itens: List<LocacaoItem> = emptyList(),
    val valorLocacao: Double = 0.0,
    val periodo: PeriodoLocacao = PeriodoLocacao.DIARIO,
    val momentoPagamento: MomentoPagamento = MomentoPagamento.NO_VENCIMENTO,
    val dataInicio: Long = currentTimeMillis(),
    val dataFimPrevista: Long = currentTimeMillis(),
    val incluiSabado: Boolean = false,
    val incluiDomingo: Boolean = false,
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
    val criadoEm: Long = currentTimeMillis(),
    val atualizadoEm: Long = currentTimeMillis()
) {
    /**
     * Retorna a lista de IDs dos equipamentos.
     * Faz migração automática de dados antigos para o novo formato.
     * Prioridade: itens (v3) > equipamentoIds (v2) > equipamentoId (v1)
     */
    fun getEquipamentoIdsList(): List<String> {
        return when {
            itens.isNotEmpty() -> itens.map { it.equipamentoId }
            equipamentoIds.isNotEmpty() -> equipamentoIds
            equipamentoId.isNotEmpty() -> listOf(equipamentoId)
            else -> emptyList()
        }
    }

    /**
     * Retorna a lista de itens da locação.
     * Faz migração automática de dados antigos para o novo formato.
     */
    fun getItensList(): List<LocacaoItem> {
        return when {
            itens.isNotEmpty() -> itens
            equipamentoIds.isNotEmpty() -> equipamentoIds.map { LocacaoItem(equipamentoId = it) }
            equipamentoId.isNotEmpty() -> listOf(LocacaoItem(equipamentoId = equipamentoId))
            else -> emptyList()
        }
    }

    companion object {
        const val COLLECTION_NAME = "locacoes"
        const val DIAS_ALERTA_VENCIMENTO = 2
    }
}
