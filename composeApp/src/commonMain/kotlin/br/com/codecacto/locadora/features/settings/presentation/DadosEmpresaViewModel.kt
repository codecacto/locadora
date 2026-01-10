package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.DadosEmpresa
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import br.com.codecacto.locadora.core.ui.util.isValidCpf
import br.com.codecacto.locadora.core.ui.util.isValidCnpj
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DadosEmpresaViewModel(
    private val dadosEmpresaRepository: DadosEmpresaRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<DadosEmpresaContract.State, DadosEmpresaContract.Effect, DadosEmpresaContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(DadosEmpresaContract.State())
    override val state: StateFlow<DadosEmpresaContract.State> = _state.asStateFlow()

    init {
        loadDadosEmpresa()
    }

    private fun loadDadosEmpresa() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            dadosEmpresaRepository.getDadosEmpresa().collect { dados ->
                val tipoPessoa = try {
                    TipoPessoa.valueOf(dados.tipoPessoa)
                } catch (e: Exception) {
                    TipoPessoa.JURIDICA
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    nomeEmpresa = dados.nomeEmpresa,
                    telefone = dados.telefone,
                    email = dados.email,
                    endereco = dados.endereco,
                    documento = dados.documento,
                    tipoPessoa = tipoPessoa
                )
            }
        }
    }

    override fun onAction(action: DadosEmpresaContract.Action) {
        when (action) {
            is DadosEmpresaContract.Action.SetNomeEmpresa -> {
                _state.value = _state.value.copy(nomeEmpresa = action.value)
            }
            is DadosEmpresaContract.Action.SetTelefone -> {
                _state.value = _state.value.copy(telefone = action.value)
            }
            is DadosEmpresaContract.Action.SetEmail -> {
                _state.value = _state.value.copy(email = action.value)
            }
            is DadosEmpresaContract.Action.SetEndereco -> {
                _state.value = _state.value.copy(endereco = action.value)
            }
            is DadosEmpresaContract.Action.SetDocumento -> {
                _state.value = _state.value.copy(
                    documento = action.value,
                    documentoError = null
                )
            }
            is DadosEmpresaContract.Action.SetTipoPessoa -> {
                _state.value = _state.value.copy(
                    tipoPessoa = action.value,
                    documento = "", // Limpa o documento ao trocar o tipo
                    documentoError = null
                )
            }
            is DadosEmpresaContract.Action.Save -> saveDadosEmpresa()
        }
    }

    private fun validateDocumento(): Boolean {
        val documento = _state.value.documento
        if (documento.isBlank()) return true // Campo opcional

        val isValid = when (_state.value.tipoPessoa) {
            TipoPessoa.FISICA -> isValidCpf(documento)
            TipoPessoa.JURIDICA -> isValidCnpj(documento)
        }

        if (!isValid) {
            val errorMessage = when (_state.value.tipoPessoa) {
                TipoPessoa.FISICA -> Strings.DADOS_COMPROVANTE_CPF_INVALIDO
                TipoPessoa.JURIDICA -> Strings.DADOS_COMPROVANTE_CNPJ_INVALIDO
            }
            _state.value = _state.value.copy(documentoError = errorMessage)
            return false
        }

        return true
    }

    private fun saveDadosEmpresa() {
        if (!validateDocumento()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                val dados = DadosEmpresa(
                    nomeEmpresa = _state.value.nomeEmpresa,
                    telefone = _state.value.telefone,
                    email = _state.value.email,
                    endereco = _state.value.endereco,
                    documento = _state.value.documento,
                    tipoPessoa = _state.value.tipoPessoa.name
                )
                dadosEmpresaRepository.saveDadosEmpresa(dados)
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(DadosEmpresaContract.Effect.ShowSuccess(Strings.DADOS_COMPROVANTE_SUCESSO))
                emitEffect(DadosEmpresaContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(DadosEmpresaContract.Effect.ShowError(e.message ?: Strings.DADOS_COMPROVANTE_ERRO))
            }
        }
    }
}
