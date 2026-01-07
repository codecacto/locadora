package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                    precoPadraoLocacao = "",
                    valorCompra = "",
                    observacoes = ""
                )
            }
            is EquipamentosContract.Action.HideForm -> {
                _state.value = _state.value.copy(showForm = false, editingEquipamento = null)
            }
            is EquipamentosContract.Action.EditEquipamento -> {
                _state.value = _state.value.copy(
                    showForm = true,
                    editingEquipamento = action.equipamento,
                    nome = action.equipamento.nome,
                    categoria = action.equipamento.categoria,
                    identificacao = action.equipamento.identificacao ?: "",
                    precoPadraoLocacao = action.equipamento.precoPadraoLocacao.toString(),
                    valorCompra = action.equipamento.valorCompra?.toString() ?: "",
                    observacoes = action.equipamento.observacoes ?: ""
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
            is EquipamentosContract.Action.SetPrecoPadraoLocacao -> {
                _state.value = _state.value.copy(precoPadraoLocacao = action.value)
            }
            is EquipamentosContract.Action.SetValorCompra -> {
                _state.value = _state.value.copy(valorCompra = action.value)
            }
            is EquipamentosContract.Action.SetObservacoes -> {
                _state.value = _state.value.copy(observacoes = action.value)
            }
            is EquipamentosContract.Action.SaveEquipamento -> saveEquipamento()
            is EquipamentosContract.Action.Refresh -> loadEquipamentos()
        }
    }

    private fun loadEquipamentos() {
        viewModelScope.launch {
            try {
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
                }.collect { equipamentos ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        equipamentos = equipamentos,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                handleError(e)
            }
        }
    }

    private fun saveEquipamento() {
        val currentState = _state.value

        if (currentState.nome.isBlank()) {
            emitEffect(EquipamentosContract.Effect.ShowError("Nome do equipamento é obrigatório"))
            return
        }

        if (currentState.categoria.isBlank()) {
            emitEffect(EquipamentosContract.Effect.ShowError("Categoria é obrigatória"))
            return
        }

        val precoPadraoLocacao = currentState.precoPadraoLocacao.toDoubleOrNull()
        if (precoPadraoLocacao == null || precoPadraoLocacao <= 0) {
            emitEffect(EquipamentosContract.Effect.ShowError("Preço de locação inválido"))
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                val equipamento = Equipamento(
                    id = currentState.editingEquipamento?.id ?: "",
                    nome = currentState.nome,
                    categoria = currentState.categoria,
                    identificacao = currentState.identificacao.ifBlank { null },
                    precoPadraoLocacao = precoPadraoLocacao,
                    valorCompra = currentState.valorCompra.toDoubleOrNull(),
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
