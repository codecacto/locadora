package br.com.codecacto.locadora.core.model

import br.com.codecacto.locadora.currentTimeMillis
import kotlinx.serialization.Serializable

@Serializable
data class Equipamento(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val identificacao: String? = null,  // Mantido para retrocompatibilidade
    val valorCompra: Double? = null,
    val precoDiario: Double? = null,
    val precoSemanal: Double? = null,
    val precoQuinzenal: Double? = null,
    val precoMensal: Double? = null,
    val observacoes: String? = null,
    // Novos campos para controle de quantidade e patrimônios
    val quantidade: Int = 1,  // Quantidade total deste equipamento
    val usaPatrimonio: Boolean = false,  // Se true, rastreia unidades individualmente
    val patrimonios: List<Patrimonio> = emptyList(),  // Lista de patrimônios (se usaPatrimonio = true)
    val criadoEm: Long = currentTimeMillis(),
    val atualizadoEm: Long = currentTimeMillis()
) {
    fun getPeriodosDisponiveis(): List<PeriodoLocacao> {
        return buildList {
            if (precoDiario != null && precoDiario > 0) add(PeriodoLocacao.DIARIO)
            if (precoSemanal != null && precoSemanal > 0) add(PeriodoLocacao.SEMANAL)
            if (precoQuinzenal != null && precoQuinzenal > 0) add(PeriodoLocacao.QUINZENAL)
            if (precoMensal != null && precoMensal > 0) add(PeriodoLocacao.MENSAL)
        }
    }

    fun getPreco(periodo: PeriodoLocacao): Double? {
        return when (periodo) {
            PeriodoLocacao.DIARIO -> precoDiario
            PeriodoLocacao.SEMANAL -> precoSemanal
            PeriodoLocacao.QUINZENAL -> precoQuinzenal
            PeriodoLocacao.MENSAL -> precoMensal
        }
    }

    fun hasAtLeastOnePrice(): Boolean {
        return (precoDiario != null && precoDiario > 0) ||
                (precoSemanal != null && precoSemanal > 0) ||
                (precoQuinzenal != null && precoQuinzenal > 0) ||
                (precoMensal != null && precoMensal > 0)
    }

    fun getPrimeiroPrecoDisponivel(): Pair<PeriodoLocacao, Double>? {
        precoDiario?.takeIf { it > 0 }?.let { return PeriodoLocacao.DIARIO to it }
        precoSemanal?.takeIf { it > 0 }?.let { return PeriodoLocacao.SEMANAL to it }
        precoQuinzenal?.takeIf { it > 0 }?.let { return PeriodoLocacao.QUINZENAL to it }
        precoMensal?.takeIf { it > 0 }?.let { return PeriodoLocacao.MENSAL to it }
        return null
    }

    companion object {
        const val COLLECTION_NAME = "equipamentos"
    }
}
