package br.com.codecacto.locadora.features.locacoes.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.DisponibilidadeEquipamento
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.currentTimeMillis

object NovaLocacaoContract {

    /**
     * Representa um item selecionado para a locação com quantidade e patrimônios específicos.
     */
    data class ItemSelecionado(
        val equipamento: Equipamento,
        val quantidade: Int = 1,
        val patrimonioIds: List<String> = emptyList()
    )

    data class State(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val clientes: List<Cliente> = emptyList(),
        val equipamentosDisponiveis: List<Equipamento> = emptyList(),
        val clienteSelecionado: Cliente? = null,
        // Novo: itens selecionados com quantidade e patrimônios
        val itensSelecionados: List<ItemSelecionado> = emptyList(),
        // Disponibilidade de cada equipamento selecionado
        val disponibilidades: Map<String, DisponibilidadeEquipamento> = emptyMap(),
        val periodosDisponiveis: List<PeriodoLocacao> = emptyList(),
        val periodoSelecionado: PeriodoLocacao? = null,
        val valorLocacao: String = "",
        val dataInicio: Long = currentTimeMillis(),
        val dataFimPrevista: Long? = null,
        val dataVencimentoPagamento: Long? = null,
        val statusEntrega: StatusEntrega = StatusEntrega.NAO_AGENDADA,
        val dataEntregaPrevista: Long? = null,
        val emitirNota: Boolean = false,
        val incluiSabado: Boolean = false,
        val incluiDomingo: Boolean = false,
        val diasCalculados: Int = 0,
        val error: String? = null
    ) : UiState {
        // Helper para compatibilidade - retorna os equipamentos dos itens selecionados
        val equipamentosSelecionados: List<Equipamento>
            get() = itensSelecionados.map { it.equipamento }
    }

    sealed class Action : UiAction {
        data class SelectCliente(val cliente: Cliente) : Action()
        data class AddEquipamento(val equipamento: Equipamento) : Action()
        data class RemoveEquipamento(val equipamentoId: String) : Action()
        // Novas actions para quantidade e patrimônios
        data class SetQuantidadeItem(val equipamentoId: String, val quantidade: Int) : Action()
        data class TogglePatrimonio(val equipamentoId: String, val patrimonioId: String) : Action()
        data class SetPeriodo(val periodo: PeriodoLocacao) : Action()
        data class SetValorLocacao(val valor: String) : Action()
        data class SetDataInicio(val data: Long) : Action()
        data class SetDataFimPrevista(val data: Long) : Action()
        data class SetDataVencimentoPagamento(val data: Long) : Action()
        data class SetStatusEntrega(val status: StatusEntrega) : Action()
        data class SetDataEntregaPrevista(val data: Long) : Action()
        data class SetEmitirNota(val emitir: Boolean) : Action()
        data class SetIncluiSabado(val inclui: Boolean) : Action()
        data class SetIncluiDomingo(val inclui: Boolean) : Action()
        data object CriarLocacao : Action()
        data object ReloadData : Action()
        data object ClearForm : Action()
    }

    sealed interface Effect : UiEffect {
        data object LocacaoCriada : Effect
        data class ShowError(val message: String) : Effect
    }
}
