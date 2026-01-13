package br.com.codecacto.locadora.features.equipamentos.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LocacaoFaturamento(
    val locacao: Locacao,
    val clienteNome: String
)

data class MesAnoFaturamento(
    val mes: Int,
    val ano: Int
) {
    override fun toString(): String = "$mes/$ano"
}

object FaturamentoEquipamentoContract {
    data class State(
        val isLoading: Boolean = true,
        val equipamento: Equipamento? = null,
        val locacoesPagas: List<LocacaoFaturamento> = emptyList(),
        val mesesDisponiveis: List<MesAnoFaturamento> = emptyList(),
        val mesSelecionado: MesAnoFaturamento? = null,
        val faturamentoTotal: Double = 0.0,
        val faturamentoFiltrado: Double = 0.0,
        val valorCompra: Double = 0.0,
        val lucroTotal: Double = 0.0,
        val lucroFiltrado: Double = 0.0,
        val error: String? = null
    ) : UiState {
        val locacoesFiltradas: List<LocacaoFaturamento>
            get() = if (mesSelecionado == null) {
                locacoesPagas
            } else {
                locacoesPagas.filter { locacao ->
                    val instant = Instant.fromEpochMilliseconds(
                        locacao.locacao.dataPagamento ?: locacao.locacao.dataFimPrevista
                    )
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    localDate.monthNumber == mesSelecionado.mes && localDate.year == mesSelecionado.ano
                }
            }
    }

    sealed class Action : UiAction {
        data class LoadData(val equipamentoId: String) : Action()
        data class SelectMes(val mesAno: MesAnoFaturamento?) : Action()
    }

    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}
