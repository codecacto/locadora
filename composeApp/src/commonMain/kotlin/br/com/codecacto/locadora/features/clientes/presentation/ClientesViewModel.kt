package br.com.codecacto.locadora.features.clientes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import br.com.codecacto.locadora.core.ui.util.isValidEmail
import br.com.codecacto.locadora.core.ui.util.isValidCpf
import br.com.codecacto.locadora.core.ui.util.isValidCnpj
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
                    tipoPessoa = TipoPessoa.FISICA,
                    cpfCnpj = "",
                    telefoneWhatsapp = "",
                    email = "",
                    endereco = "",
                    precisaNotaFiscalPadrao = false,
                    emailError = null,
                    cpfCnpjError = null,
                    telefoneError = null,
                    isSaving = false
                )
            }
            is ClientesContract.Action.HideForm -> {
                _state.value = _state.value.copy(showForm = false, editingCliente = null)
            }
            is ClientesContract.Action.EditCliente -> {
                val cpfCnpjDigits = action.cliente.cpfCnpj?.filter { it.isLetterOrDigit() } ?: ""
                val tipoPessoa = if (cpfCnpjDigits.length > 11) TipoPessoa.JURIDICA else TipoPessoa.FISICA
                _state.value = _state.value.copy(
                    showForm = true,
                    editingCliente = action.cliente,
                    nomeRazao = action.cliente.nomeRazao,
                    tipoPessoa = tipoPessoa,
                    cpfCnpj = cpfCnpjDigits,
                    telefoneWhatsapp = action.cliente.telefoneWhatsapp.filter { it.isDigit() },
                    email = action.cliente.email ?: "",
                    endereco = action.cliente.endereco ?: "",
                    precisaNotaFiscalPadrao = action.cliente.precisaNotaFiscalPadrao,
                    emailError = null,
                    cpfCnpjError = null,
                    telefoneError = null,
                    isSaving = false
                )
            }
            is ClientesContract.Action.DeleteCliente -> deleteCliente(action.cliente)
            is ClientesContract.Action.SetNomeRazao -> {
                _state.value = _state.value.copy(nomeRazao = action.value)
            }
            is ClientesContract.Action.SetTipoPessoa -> {
                _state.value = _state.value.copy(
                    tipoPessoa = action.value,
                    cpfCnpj = "", // Clear CPF/CNPJ when changing type
                    cpfCnpjError = null
                )
            }
            is ClientesContract.Action.SetCpfCnpj -> {
                val currentTipo = _state.value.tipoPessoa
                val cpfCnpjError = validateCpfCnpjOnComplete(action.value, currentTipo)
                _state.value = _state.value.copy(cpfCnpj = action.value, cpfCnpjError = cpfCnpjError)
            }
            is ClientesContract.Action.SetTelefoneWhatsapp -> {
                _state.value = _state.value.copy(telefoneWhatsapp = action.value, telefoneError = null)
            }
            is ClientesContract.Action.SetEmail -> {
                val emailError = if (action.value.isNotBlank() && !isValidEmail(action.value)) {
                    "Email invalido"
                } else null
                _state.value = _state.value.copy(email = action.value, emailError = emailError)
            }
            is ClientesContract.Action.SetEndereco -> {
                _state.value = _state.value.copy(endereco = action.value)
            }
            is ClientesContract.Action.SetPrecisaNotaFiscal -> {
                _state.value = _state.value.copy(precisaNotaFiscalPadrao = action.value)
            }
            is ClientesContract.Action.SaveCliente -> saveCliente()
            is ClientesContract.Action.Refresh -> refreshClientes()
            is ClientesContract.Action.ClearForm -> {
                _state.value = _state.value.copy(
                    editingCliente = null,
                    nomeRazao = "",
                    tipoPessoa = TipoPessoa.FISICA,
                    cpfCnpj = "",
                    telefoneWhatsapp = "",
                    email = "",
                    endereco = "",
                    precisaNotaFiscalPadrao = false,
                    emailError = null,
                    cpfCnpjError = null,
                    telefoneError = null
                )
            }
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

    private fun refreshClientes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(500)
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    private fun saveCliente() {
        val currentState = _state.value

        if (currentState.nomeRazao.isBlank()) {
            emitEffect(ClientesContract.Effect.ShowError("Nome/Razao Social e obrigatorio"))
            return
        }

        val phoneDigits = currentState.telefoneWhatsapp.filter { it.isDigit() }
        if (phoneDigits.length < 10) {
            _state.value = _state.value.copy(telefoneError = "Telefone deve ter pelo menos 10 digitos")
            emitEffect(ClientesContract.Effect.ShowError("Telefone/WhatsApp invalido"))
            return
        }

        if (currentState.email.isNotBlank() && !isValidEmail(currentState.email)) {
            _state.value = _state.value.copy(emailError = "Email invalido")
            emitEffect(ClientesContract.Effect.ShowError("Email invalido"))
            return
        }

        // Validate CPF/CNPJ if provided
        val cpfCnpjDigits = currentState.cpfCnpj.filter { it.isLetterOrDigit() }
        if (cpfCnpjDigits.isNotBlank()) {
            if (currentState.tipoPessoa == TipoPessoa.FISICA) {
                if (!isValidCpf(cpfCnpjDigits)) {
                    _state.value = _state.value.copy(cpfCnpjError = "CPF invalido")
                    emitEffect(ClientesContract.Effect.ShowError("CPF invalido"))
                    return
                }
            } else {
                if (!isValidCnpj(cpfCnpjDigits)) {
                    _state.value = _state.value.copy(cpfCnpjError = "CNPJ invalido")
                    emitEffect(ClientesContract.Effect.ShowError("CNPJ invalido"))
                    return
                }
            }
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                // Format phone for storage
                val formattedPhone = formatPhoneForStorage(phoneDigits)

                val cliente = Cliente(
                    id = currentState.editingCliente?.id ?: "",
                    nomeRazao = currentState.nomeRazao,
                    cpfCnpj = cpfCnpjDigits.ifBlank { null },
                    telefoneWhatsapp = formattedPhone,
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

    private fun formatPhoneForStorage(digits: String): String {
        return if (digits.length == 11) {
            "(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}"
        } else if (digits.length == 10) {
            "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}"
        } else {
            digits
        }
    }

    private fun validateCpfCnpjOnComplete(value: String, tipoPessoa: TipoPessoa): String? {
        if (value.isBlank()) return null

        return if (tipoPessoa == TipoPessoa.FISICA) {
            // CPF has 11 digits - validate when complete
            if (value.length == 11 && !isValidCpf(value)) {
                "CPF invalido"
            } else null
        } else {
            // CNPJ has 14 characters - validate when complete
            if (value.length == 14 && !isValidCnpj(value)) {
                "CNPJ invalido"
            } else null
        }
    }
}
