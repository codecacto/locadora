package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    private val locacaoRepository: LocacaoRepository,
    private val recebimentoRepository: RecebimentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<SettingsContract.State, SettingsContract.Effect, SettingsContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(SettingsContract.State())
    override val state: StateFlow<SettingsContract.State> = _state.asStateFlow()

    init {
        loadCurrentEmail()
    }

    private fun loadCurrentEmail() {
        val email = authRepository.currentUser?.email ?: ""
        _state.value = _state.value.copy(currentEmail = email)
    }

    override fun onAction(action: SettingsContract.Action) {
        when (action) {
            is SettingsContract.Action.DeleteAllData -> deleteAllData()
        }
    }

    private fun deleteAllData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isDeletingAllData = true)

                // Deletar todos os recebimentos
                val recebimentos = recebimentoRepository.getRecebimentos().first()
                recebimentos.forEach { recebimento ->
                    recebimentoRepository.deleteRecebimento(recebimento.id)
                }

                // Deletar todas as locações
                val locacoes = locacaoRepository.getLocacoes().first()
                locacoes.forEach { locacao ->
                    locacaoRepository.deleteLocacao(locacao.id)
                }

                // Deletar todos os equipamentos
                val equipamentos = equipamentoRepository.getEquipamentos().first()
                equipamentos.forEach { equipamento ->
                    equipamentoRepository.deleteEquipamento(equipamento.id)
                }

                // Deletar todos os clientes
                val clientes = clienteRepository.getClientes().first()
                clientes.forEach { cliente ->
                    clienteRepository.deleteCliente(cliente.id)
                }

                _state.value = _state.value.copy(isDeletingAllData = false)
                emitEffect(SettingsContract.Effect.ShowSuccess("Todos os dados foram apagados com sucesso!"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isDeletingAllData = false)
                handleError(e)
                emitEffect(SettingsContract.Effect.ShowError(e.message ?: "Erro ao apagar dados"))
            }
        }
    }
}
