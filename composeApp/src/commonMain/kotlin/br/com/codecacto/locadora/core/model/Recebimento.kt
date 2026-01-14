package br.com.codecacto.locadora.core.model

import br.com.codecacto.locadora.currentTimeMillis
import kotlinx.serialization.Serializable

@Serializable
data class Recebimento(
    val id: String = "",
    val locacaoId: String = "",
    val clienteId: String = "",
    // Campo antigo mantido para compatibilidade com dados existentes (v1)
    val equipamentoId: String = "",
    // Campo para múltiplos equipamentos (v2)
    val equipamentoIds: List<String> = emptyList(),
    // Novo campo com itens detalhados - quantidade e patrimônios (v3)
    val itens: List<LocacaoItem> = emptyList(),
    val valor: Double = 0.0,
    val dataVencimento: Long = currentTimeMillis(),
    val status: StatusPagamento = StatusPagamento.PENDENTE,
    val dataPagamento: Long? = null,
    val numeroRenovacao: Int = 0, // 0 = locação original, 1+ = renovações
    val criadoEm: Long = currentTimeMillis()
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
     * Retorna a lista de itens do recebimento.
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
        const val COLLECTION_NAME = "recebimentos"
    }
}
