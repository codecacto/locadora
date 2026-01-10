package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega

object NovaLocacaoContract {
    data class State(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val clientes: List<Cliente> = emptyList(),
        val equipamentosDisponiveis: List<Equipamento> = emptyList(),
        val clienteSelecionado: Cliente? = null,
        val equipamentoSelecionado: Equipamento? = null,
        val periodosDisponiveis: List<PeriodoLocacao> = emptyList(),
        val periodoSelecionado: PeriodoLocacao? = null,
        val valorLocacao: String = "",
        val dataInicio: Long = System.currentTimeMillis(),
        val dataFimPrevista: Long? = null,
        val dataVencimentoPagamento: Long? = null,
        val statusEntrega: StatusEntrega = StatusEntrega.NAO_AGENDADA,
        val dataEntregaPrevista: Long? = null,
        val emitirNota: Boolean = false,
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class SelectCliente(val cliente: Cliente) : Action()
        data class SelectEquipamento(val equipamento: Equipamento) : Action()
        data class SetPeriodo(val periodo: PeriodoLocacao) : Action()
        data class SetValorLocacao(val valor: String) : Action()
        data class SetDataInicio(val data: Long) : Action()
        data class SetDataFimPrevista(val data: Long) : Action()
        data class SetDataVencimentoPagamento(val data: Long) : Action()
        data class SetStatusEntrega(val status: StatusEntrega) : Action()
        data class SetDataEntregaPrevista(val data: Long) : Action()
        data class SetEmitirNota(val emitir: Boolean) : Action()
        data object CriarLocacao : Action()
        data object ReloadData : Action()
        data object ClearForm : Action()
    }

    sealed interface Effect : UiEffect {
        data object LocacaoCriada : Effect
        data class ShowError(val message: String) : Effect
    }
}
