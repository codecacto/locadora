package br.com.codecacto.locadora.features.clientes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.data.repository.ClienteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientesViewModel(
    private val clienteRepository: ClienteRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<ClientesContract.State, ClientesContract.Effect, ClientesContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(ClientesContract.State())
    override val state: StateFlow<ClientesContract.State> = _state.asStateFlow()

    init {
        loadClientes()
    }

    override fun onAction(action: ClientesContract.Action) {
        when (action) {
            is ClientesContract.Action.Search -> {
                _state.value = _state.value.copy(searchQuery = action.query)
            }
            is ClientesContract.Action.ShowForm -> {
                _state.value = _state.value.copy(
                    showForm = true,
                    editingCliente = null,
                    nomeRazao = "",
                    cpfCnpj = "",
                    telefoneWhatsapp = "",
                    email = "",
                    endereco = "",
                    precisaNotaFiscalPadrao = false
                )
            }
            is ClientesContract.Action.HideForm -> {
                _state.value = _state.value.copy(showForm = false, editingCliente = null)
            }
            is ClientesContract.Action.EditCliente -> {
                _state.value = _state.value.copy(
                    showForm = true,
                    editingCliente = action.cliente,
                    nomeRazao = action.cliente.nomeRazao,
                    cpfCnpj = action.cliente.cpfCnpj ?: "",
                    telefoneWhatsapp = action.cliente.telefoneWhatsapp,
                    email = action.cliente.email ?: "",
                    endereco = action.cliente.endereco ?: "",
                    precisaNotaFiscalPadrao = action.cliente.precisaNotaFiscalPadrao
                )
            }
            is ClientesContract.Action.DeleteCliente -> deleteCliente(action.cliente)
            is ClientesContract.Action.SetNomeRazao -> {
                _state.value = _state.value.copy(nomeRazao = action.value)
            }
            is ClientesContract.Action.SetCpfCnpj -> {
                _state.value = _state.value.copy(cpfCnpj = action.value)
            }
            is ClientesContract.Action.SetTelefoneWhatsapp -> {
                _state.value = _state.value.copy(telefoneWhatsapp = action.value)
            }
            is ClientesContract.Action.SetEmail -> {
                _state.value = _state.value.copy(email = action.value)
            }
            is ClientesContract.Action.SetEndereco -> {
                _state.value = _state.value.copy(endereco = action.value)
            }
            is ClientesContract.Action.SetPrecisaNotaFiscal -> {
                _state.value = _state.value.copy(precisaNotaFiscalPadrao = action.value)
            }
            is ClientesContract.Action.SaveCliente -> saveCliente()
            is ClientesContract.Action.Refresh -> loadClientes()
        }
    }

    private fun loadClientes() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                clienteRepository.getClientes().collect { clientes ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        clientes = clientes,
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

    private fun saveCliente() {
        val currentState = _state.value

        if (currentState.nomeRazao.isBlank()) {
            emitEffect(ClientesContract.Effect.ShowError("Nome/Razão Social é obrigatório"))
            return
        }

        if (currentState.telefoneWhatsapp.isBlank()) {
            emitEffect(ClientesContract.Effect.ShowError("Telefone/WhatsApp é obrigatório"))
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                val cliente = Cliente(
                    id = currentState.editingCliente?.id ?: "",
                    nomeRazao = currentState.nomeRazao,
                    cpfCnpj = currentState.cpfCnpj.ifBlank { null },
                    telefoneWhatsapp = currentState.telefoneWhatsapp,
                    email = currentState.email.ifBlank { null },
                    endereco = currentState.endereco.ifBlank { null },
                    precisaNotaFiscalPadrao = currentState.precisaNotaFiscalPadrao
                )

                if (currentState.editingCliente != null) {
                    clienteRepository.updateCliente(cliente)
                    emitEffect(ClientesContract.Effect.ShowSuccess("Cliente atualizado com sucesso!"))
                } else {
                    clienteRepository.addCliente(cliente)
                    emitEffect(ClientesContract.Effect.ShowSuccess("Cliente cadastrado com sucesso!"))
                }

                _state.value = _state.value.copy(
                    isSaving = false,
                    showForm = false,
                    editingCliente = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                handleError(e)
                emitEffect(ClientesContract.Effect.ShowError(e.message ?: "Erro ao salvar cliente"))
            }
        }
    }

    private fun deleteCliente(cliente: Cliente) {
        viewModelScope.launch {
            try {
                clienteRepository.deleteCliente(cliente.id)
                emitEffect(ClientesContract.Effect.ShowSuccess("Cliente excluído com sucesso!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(ClientesContract.Effect.ShowError(e.message ?: "Erro ao excluir cliente"))
            }
        }
    }
}
