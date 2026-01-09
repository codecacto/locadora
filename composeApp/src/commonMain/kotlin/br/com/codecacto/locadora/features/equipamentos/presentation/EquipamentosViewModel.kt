package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.util.currencyToDouble
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EquipamentosViewModel(
    private val equipamentoRepository: EquipamentoRepository,
    private val locacaoRepository: LocacaoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<EquipamentosContract.State, EquipamentosContract.Effect, EquipamentosContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(EquipamentosContract.State())
    override val state: StateFlow<EquipamentosContract.State> = _state.asStateFlow()

    init {
        loadEquipamentos()
    }

    override fun onAction(action: EquipamentosContract.Action) {
        when (action) {
            is EquipamentosContract.Action.Search -> {
                _state.value = _state.value.copy(searchQuery = action.query)
            }
            is EquipamentosContract.Action.ShowForm -> {
                _state.value = _state.value.copy(
                    showForm = true,
                    editingEquipamento = null,
                    nome = "",
                    categoria = "",
                    identificacao = "",
                    precoDiario = "",
                    precoSemanal = "",
                    precoQuinzenal = "",
                    precoMensal = "",
                    valorCompra = "",
                    observacoes = "",
                    isSaving = false
                )
            }
            is EquipamentosContract.Action.HideForm -> {
                _state.value = _state.value.copy(showForm = false, editingEquipamento = null)
            }
            is EquipamentosContract.Action.EditEquipamento -> {
                // Convert prices to cents format for masked input
                val precoDiarioInCents = action.equipamento.precoDiario?.let { (it * 100).toLong().toString() } ?: ""
                val precoSemanalInCents = action.equipamento.precoSemanal?.let { (it * 100).toLong().toString() } ?: ""
                val precoQuinzenalInCents = action.equipamento.precoQuinzenal?.let { (it * 100).toLong().toString() } ?: ""
                val precoMensalInCents = action.equipamento.precoMensal?.let { (it * 100).toLong().toString() } ?: ""
                val valorCompraInCents = action.equipamento.valorCompra?.let { (it * 100).toLong().toString() } ?: ""
                _state.value = _state.value.copy(
                    showForm = true,
                    editingEquipamento = action.equipamento,
                    nome = action.equipamento.nome,
                    categoria = action.equipamento.categoria,
                    identificacao = action.equipamento.identificacao ?: "",
                    precoDiario = precoDiarioInCents,
                    precoSemanal = precoSemanalInCents,
                    precoQuinzenal = precoQuinzenalInCents,
                    precoMensal = precoMensalInCents,
                    valorCompra = valorCompraInCents,
                    observacoes = action.equipamento.observacoes ?: "",
                    isSaving = false
                )
            }
            is EquipamentosContract.Action.DeleteEquipamento -> deleteEquipamento(action.equipamento)
            is EquipamentosContract.Action.SetNome -> {
                _state.value = _state.value.copy(nome = action.value)
            }
            is EquipamentosContract.Action.SetCategoria -> {
                _state.value = _state.value.copy(categoria = action.value)
            }
            is EquipamentosContract.Action.SetIdentificacao -> {
                _state.value = _state.value.copy(identificacao = action.value)
            }
            is EquipamentosContract.Action.SetPrecoDiario -> {
                _state.value = _state.value.copy(precoDiario = action.value)
            }
            is EquipamentosContract.Action.SetPrecoSemanal -> {
                _state.value = _state.value.copy(precoSemanal = action.value)
            }
            is EquipamentosContract.Action.SetPrecoQuinzenal -> {
                _state.value = _state.value.copy(precoQuinzenal = action.value)
            }
            is EquipamentosContract.Action.SetPrecoMensal -> {
                _state.value = _state.value.copy(precoMensal = action.value)
            }
            is EquipamentosContract.Action.SetValorCompra -> {
                _state.value = _state.value.copy(valorCompra = action.value)
            }
            is EquipamentosContract.Action.SetObservacoes -> {
                _state.value = _state.value.copy(observacoes = action.value)
            }
            is EquipamentosContract.Action.SaveEquipamento -> saveEquipamento()
            is EquipamentosContract.Action.Refresh -> refreshEquipamentos()
            is EquipamentosContract.Action.ClearForm -> {
                _state.value = _state.value.copy(
                    editingEquipamento = null,
                    nome = "",
                    categoria = "",
                    identificacao = "",
                    precoDiario = "",
                    precoSemanal = "",
                    precoQuinzenal = "",
                    precoMensal = "",
                    valorCompra = "",
                    observacoes = ""
                )
            }
        }
    }

    private fun loadEquipamentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            combine(
                equipamentoRepository.getEquipamentos(),
                locacaoRepository.getLocacoesAtivas()
            ) { equipamentos, locacoesAtivas ->
                val equipamentosAlugadosIds = locacoesAtivas
                    .filter { it.statusLocacao == StatusLocacao.ATIVA }
                    .map { it.equipamentoId }
                    .toSet()

                equipamentos.map { equipamento ->
                    EquipamentoComStatus(
                        equipamento = equipamento,
                        isAlugado = equipamento.id in equipamentosAlugadosIds
                    )
                }
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar equipamentos"
                    )
                    handleError(e)
                }
                .collect { equipamentos ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        equipamentos = equipamentos,
                        error = null
                    )
                }
        }
    }

    private fun refreshEquipamentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun saveEquipamento() {
        val currentState = _state.value

        if (currentState.categoria.isBlank()) {
            emitEffect(EquipamentosContract.Effect.ShowError("Selecione o tipo de equipamento"))
            return
        }

        if (currentState.nome.isBlank()) {
            emitEffect(EquipamentosContract.Effect.ShowError("Nome do equipamento é obrigatório"))
            return
        }

        val precoDiario = currentState.precoDiario.currencyToDouble().takeIf { it > 0 }
        val precoSemanal = currentState.precoSemanal.currencyToDouble().takeIf { it > 0 }
        val precoQuinzenal = currentState.precoQuinzenal.currencyToDouble().takeIf { it > 0 }
        val precoMensal = currentState.precoMensal.currencyToDouble().takeIf { it > 0 }

        if (precoDiario == null && precoSemanal == null && precoQuinzenal == null && precoMensal == null) {
            emitEffect(EquipamentosContract.Effect.ShowError(Strings.VALIDATION_PELO_MENOS_UM_PRECO))
            return
        }

        val valorCompra = if (currentState.valorCompra.isNotBlank()) {
            currentState.valorCompra.currencyToDouble().takeIf { it > 0 }
        } else null

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                val equipamento = Equipamento(
                    id = currentState.editingEquipamento?.id ?: "",
                    nome = currentState.nome,
                    categoria = currentState.categoria,
                    identificacao = currentState.identificacao.ifBlank { null },
                    precoDiario = precoDiario,
                    precoSemanal = precoSemanal,
                    precoQuinzenal = precoQuinzenal,
                    precoMensal = precoMensal,
                    valorCompra = valorCompra,
                    observacoes = currentState.observacoes.ifBlank { null }
                )

                if (currentState.editingEquipamento != null) {
                    equipamentoRepository.updateEquipamento(equipamento)
                    emitEffect(EquipamentosContract.Effect.ShowSuccess("Equipamento atualizado com sucesso!"))
                } else {
                    equipamentoRepository.addEquipamento(equipamento)
                    emitEffect(EquipamentosContract.Effect.ShowSuccess("Equipamento cadastrado com sucesso!"))
                }

                _state.value = _state.value.copy(
                    isSaving = false,
                    showForm = false,
                    editingEquipamento = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                handleError(e)
                emitEffect(EquipamentosContract.Effect.ShowError(e.message ?: "Erro ao salvar equipamento"))
            }
        }
    }

    private fun deleteEquipamento(equipamento: Equipamento) {
        viewModelScope.launch {
            try {
                // Verificar se o equipamento está alugado
                val isAlugado = locacaoRepository.isEquipamentoAlugado(equipamento.id)
                if (isAlugado) {
                    emitEffect(EquipamentosContract.Effect.ShowError("Não é possível excluir um equipamento que está alugado"))
                    return@launch
                }

                equipamentoRepository.deleteEquipamento(equipamento.id)
                emitEffect(EquipamentosContract.Effect.ShowSuccess("Equipamento excluído com sucesso!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(EquipamentosContract.Effect.ShowError(e.message ?: "Erro ao excluir equipamento"))
            }
        }
    }
}
