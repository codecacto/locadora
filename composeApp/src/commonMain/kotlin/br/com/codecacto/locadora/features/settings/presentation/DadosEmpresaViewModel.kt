package br.com.codecacto.locadora.features.settings.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.DadosEmpresa
import br.com.codecacto.locadora.core.ui.strings.Strings
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
                _state.value = _state.value.copy(
                    isLoading = false,
                    nomeEmpresa = dados.nomeEmpresa,
                    telefone = dados.telefone,
                    email = dados.email,
                    endereco = dados.endereco,
                    cnpj = dados.cnpj
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
            is DadosEmpresaContract.Action.SetCnpj -> {
                _state.value = _state.value.copy(cnpj = action.value)
            }
            is DadosEmpresaContract.Action.Save -> saveDadosEmpresa()
        }
    }

    private fun saveDadosEmpresa() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                val dados = DadosEmpresa(
                    nomeEmpresa = _state.value.nomeEmpresa,
                    telefone = _state.value.telefone,
                    email = _state.value.email,
                    endereco = _state.value.endereco,
                    cnpj = _state.value.cnpj
                )
                dadosEmpresaRepository.saveDadosEmpresa(dados)
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(DadosEmpresaContract.Effect.ShowSuccess(Strings.DADOS_EMPRESA_SUCESSO))
                emitEffect(DadosEmpresaContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                emitEffect(DadosEmpresaContract.Effect.ShowError(e.message ?: Strings.DADOS_EMPRESA_ERRO))
            }
        }
    }
}
