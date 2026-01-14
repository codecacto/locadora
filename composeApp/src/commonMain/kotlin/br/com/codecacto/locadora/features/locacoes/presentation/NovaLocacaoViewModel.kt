package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.LocacaoItem
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.util.currencyToDouble
import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.util.calcularDiasLocacao
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import br.com.codecacto.locadora.currentTimeMillis

class NovaLocacaoViewModel(
    private val locacaoRepository: LocacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    private val recebimentoRepository: RecebimentoRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<NovaLocacaoContract.State, NovaLocacaoContract.Effect, NovaLocacaoContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(NovaLocacaoContract.State())
    override val state: StateFlow<NovaLocacaoContract.State> = _state.asStateFlow()

    init {
        loadData()
    }

    override fun onAction(action: NovaLocacaoContract.Action) {
        when (action) {
            is NovaLocacaoContract.Action.SelectCliente -> {
                _state.value = _state.value.copy(
                    clienteSelecionado = action.cliente,
                    emitirNota = action.cliente.precisaNotaFiscalPadrao
                )
            }
            is NovaLocacaoContract.Action.AddEquipamento -> {
                // Verifica se o equipamento já está na lista
                if (_state.value.itensSelecionados.any { it.equipamento.id == action.equipamento.id }) {
                    return
                }

                // Carrega a disponibilidade do equipamento
                viewModelScope.launch {
                    val disponibilidade = locacaoRepository.getDisponibilidadeEquipamento(action.equipamento)

                    // Cria o item selecionado
                    val novoItem = NovaLocacaoContract.ItemSelecionado(
                        equipamento = action.equipamento,
                        quantidade = 1,
                        patrimonioIds = emptyList()
                    )

                    val novaLista = _state.value.itensSelecionados + novoItem
                    val novasDisponibilidades = _state.value.disponibilidades + (action.equipamento.id to disponibilidade)

                    _state.value = _state.value.copy(
                        itensSelecionados = novaLista,
                        disponibilidades = novasDisponibilidades
                    )
                    atualizarPeriodosDisponiveis()
                    recalcularValorLocacao()
                }
            }
            is NovaLocacaoContract.Action.RemoveEquipamento -> {
                val novaLista = _state.value.itensSelecionados.filter { it.equipamento.id != action.equipamentoId }
                val novasDisponibilidades = _state.value.disponibilidades - action.equipamentoId

                _state.value = _state.value.copy(
                    itensSelecionados = novaLista,
                    disponibilidades = novasDisponibilidades
                )
                atualizarPeriodosDisponiveis()
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetQuantidadeItem -> {
                val novaLista = _state.value.itensSelecionados.map { item ->
                    if (item.equipamento.id == action.equipamentoId) {
                        // Se equipamento usa patrimônio, quantidade é definida pelos patrimônios selecionados
                        val disponibilidade = _state.value.disponibilidades[action.equipamentoId]
                        val maxQuantidade = disponibilidade?.quantidadeDisponivel ?: item.equipamento.quantidade
                        val novaQuantidade = action.quantidade.coerceIn(1, maxQuantidade)
                        item.copy(quantidade = novaQuantidade)
                    } else item
                }
                _state.value = _state.value.copy(itensSelecionados = novaLista)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.TogglePatrimonio -> {
                val novaLista = _state.value.itensSelecionados.map { item ->
                    if (item.equipamento.id == action.equipamentoId) {
                        val novosPatrimonioIds = if (action.patrimonioId in item.patrimonioIds) {
                            item.patrimonioIds - action.patrimonioId
                        } else {
                            item.patrimonioIds + action.patrimonioId
                        }
                        // Quantidade = número de patrimônios selecionados
                        item.copy(
                            patrimonioIds = novosPatrimonioIds,
                            quantidade = novosPatrimonioIds.size.coerceAtLeast(1)
                        )
                    } else item
                }
                _state.value = _state.value.copy(itensSelecionados = novaLista)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetPeriodo -> {
                if (_state.value.itensSelecionados.isEmpty()) return
                val dataFim = calcularDataFim(_state.value.dataInicio, action.periodo)
                _state.value = _state.value.copy(
                    periodoSelecionado = action.periodo,
                    dataFimPrevista = dataFim,
                    dataVencimentoPagamento = dataFim
                )
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetValorLocacao -> {
                _state.value = _state.value.copy(valorLocacao = action.valor)
            }
            is NovaLocacaoContract.Action.SetDataInicio -> {
                val periodo = _state.value.periodoSelecionado
                // Para período DIÁRIO, não recalcula dataFim (usuário define manualmente o intervalo)
                // Para outros períodos, recalcula dataFim automaticamente
                val deveRecalcularDataFim = periodo != null && periodo != PeriodoLocacao.DIARIO
                val dataFim = if (deveRecalcularDataFim) {
                    calcularDataFim(action.data, periodo!!)
                } else {
                    _state.value.dataFimPrevista
                }
                _state.value = _state.value.copy(
                    dataInicio = action.data,
                    dataFimPrevista = dataFim,
                    dataVencimentoPagamento = dataFim ?: _state.value.dataVencimentoPagamento
                )
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetDataFimPrevista -> {
                _state.value = _state.value.copy(dataFimPrevista = action.data)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetDataVencimentoPagamento -> {
                _state.value = _state.value.copy(dataVencimentoPagamento = action.data)
            }
            is NovaLocacaoContract.Action.SetStatusEntrega -> {
                _state.value = _state.value.copy(statusEntrega = action.status)
            }
            is NovaLocacaoContract.Action.SetDataEntregaPrevista -> {
                _state.value = _state.value.copy(dataEntregaPrevista = action.data)
            }
            is NovaLocacaoContract.Action.SetEmitirNota -> {
                _state.value = _state.value.copy(emitirNota = action.emitir)
            }
            is NovaLocacaoContract.Action.SetIncluiSabado -> {
                _state.value = _state.value.copy(incluiSabado = action.inclui)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.SetIncluiDomingo -> {
                _state.value = _state.value.copy(incluiDomingo = action.inclui)
                recalcularValorLocacao()
            }
            is NovaLocacaoContract.Action.CriarLocacao -> {
                criarLocacao()
            }
            is NovaLocacaoContract.Action.ReloadData -> {
                loadData()
            }
            is NovaLocacaoContract.Action.ClearForm -> {
                clearForm()
            }
        }
    }

    private fun clearForm() {
        _state.value = _state.value.copy(
            clienteSelecionado = null,
            itensSelecionados = emptyList(),
            disponibilidades = emptyMap(),
            periodosDisponiveis = emptyList(),
            periodoSelecionado = null,
            valorLocacao = "",
            dataInicio = currentTimeMillis(),
            dataFimPrevista = null,
            dataVencimentoPagamento = null,
            statusEntrega = StatusEntrega.NAO_AGENDADA,
            dataEntregaPrevista = null,
            emitirNota = false,
            incluiSabado = false,
            incluiDomingo = false,
            diasCalculados = 0,
            error = null
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                combine(
                    clienteRepository.getClientes(),
                    equipamentoRepository.getEquipamentos(),
                    locacaoRepository.getLocacoesAtivas()
                ) { clientes, equipamentos, locacoesAtivas ->
                    // Coleta todos os IDs de equipamentos alugados (usando método de migração)
                    val equipamentosAlugadosIds = locacoesAtivas
                        .flatMap { it.getEquipamentoIdsList() }
                        .toSet()
                    val equipamentosDisponiveis = equipamentos.filter { it.id !in equipamentosAlugadosIds }
                    Triple(clientes, equipamentosDisponiveis, equipamentos)
                }.collect { (clientes, equipamentosDisponiveis, _) ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        clientes = clientes,
                        equipamentosDisponiveis = equipamentosDisponiveis
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                handleError(e)
            }
        }
    }

    private fun criarLocacao() {
        val currentState = _state.value

        if (currentState.clienteSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_CLIENTE))
            return
        }

        if (currentState.itensSelecionados.isEmpty()) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_EQUIPAMENTO))
            return
        }

        if (currentState.periodoSelecionado == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_SELECIONE_PERIODO))
            return
        }

        if (currentState.dataFimPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_DATA_FIM))
            return
        }

        val valorLocacao = currentState.valorLocacao.currencyToDouble()
        if (valorLocacao <= 0) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_VALOR))
            return
        }

        if (currentState.statusEntrega == StatusEntrega.AGENDADA && currentState.dataEntregaPrevista == null) {
            emitEffect(NovaLocacaoContract.Effect.ShowError(Strings.VALIDATION_INFORME_DATA_ENTREGA))
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true)

                // Converte itens selecionados para LocacaoItem
                val itens = currentState.itensSelecionados.map { item ->
                    LocacaoItem(
                        equipamentoId = item.equipamento.id,
                        quantidade = item.quantidade,
                        patrimonioIds = item.patrimonioIds
                    )
                }

                val locacao = Locacao(
                    clienteId = currentState.clienteSelecionado.id,
                    itens = itens,  // Usa o novo campo itens
                    valorLocacao = valorLocacao,
                    periodo = currentState.periodoSelecionado,
                    dataInicio = currentState.dataInicio,
                    dataFimPrevista = currentState.dataFimPrevista,
                    incluiSabado = currentState.incluiSabado,
                    incluiDomingo = currentState.incluiDomingo,
                    statusEntrega = currentState.statusEntrega,
                    dataEntregaPrevista = if (currentState.statusEntrega == StatusEntrega.AGENDADA) {
                        currentState.dataEntregaPrevista
                    } else null,
                    emitirNota = currentState.emitirNota
                )

                val locacaoId = locacaoRepository.addLocacao(locacao)

                // Criar recebimento para esta locação
                val recebimento = Recebimento(
                    locacaoId = locacaoId,
                    clienteId = currentState.clienteSelecionado.id,
                    itens = itens,  // Usa o novo campo itens
                    valor = valorLocacao,
                    dataVencimento = currentState.dataVencimentoPagamento ?: currentState.dataFimPrevista,
                    numeroRenovacao = 0
                )
                recebimentoRepository.addRecebimento(recebimento)

                _state.value = _state.value.copy(isSaving = false)
                emitEffect(NovaLocacaoContract.Effect.LocacaoCriada)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                handleError(e)
                emitEffect(NovaLocacaoContract.Effect.ShowError(e.message ?: Strings.ERROR_CRIAR_LOCACAO))
            }
        }
    }

    private fun calcularDataFim(dataInicio: Long, periodo: PeriodoLocacao): Long {
        val diasEmMillis = periodo.dias * 24L * 60L * 60L * 1000L
        return dataInicio + diasEmMillis
    }

    /**
     * Atualiza os períodos disponíveis baseado na interseção dos períodos de todos os equipamentos.
     * Só mostra períodos que TODOS os equipamentos selecionados suportam.
     * Mantém o período selecionado se ainda estiver disponível.
     */
    private fun atualizarPeriodosDisponiveis() {
        val itens = _state.value.itensSelecionados
        val periodoAtual = _state.value.periodoSelecionado

        if (itens.isEmpty()) {
            _state.value = _state.value.copy(
                periodosDisponiveis = emptyList(),
                periodoSelecionado = null,
                dataFimPrevista = null,
                dataVencimentoPagamento = null,
                valorLocacao = "",
                diasCalculados = 0
            )
            return
        }

        // Calcula a interseção dos períodos disponíveis de todos os equipamentos
        val periodosComuns = itens
            .map { it.equipamento.getPeriodosDisponiveis().toSet() }
            .reduce { acc, set -> acc.intersect(set) }
            .toList()
            .sortedBy { it.dias }

        // Mantém o período atual se ainda estiver disponível, senão usa o primeiro
        val novoPeriodo = if (periodoAtual != null && periodoAtual in periodosComuns) {
            periodoAtual
        } else {
            periodosComuns.firstOrNull()
        }

        val dataFim = novoPeriodo?.let {
            calcularDataFim(_state.value.dataInicio, it)
        }

        _state.value = _state.value.copy(
            periodosDisponiveis = periodosComuns,
            periodoSelecionado = novoPeriodo,
            dataFimPrevista = dataFim,
            dataVencimentoPagamento = dataFim
        )
    }

    /**
     * Recalcula o valor da locação somando os preços de todos os equipamentos selecionados,
     * considerando a quantidade de cada item.
     */
    private fun recalcularValorLocacao() {
        val currentState = _state.value
        val itens = currentState.itensSelecionados
        if (itens.isEmpty()) {
            _state.value = _state.value.copy(valorLocacao = "", diasCalculados = 0)
            return
        }

        val periodo = currentState.periodoSelecionado ?: return
        val dataFim = currentState.dataFimPrevista ?: return

        if (periodo == PeriodoLocacao.DIARIO) {
            // Para diária, calcula baseado no número de dias
            val dias = calcularDiasLocacao(
                dataInicioMillis = currentState.dataInicio,
                dataFimMillis = dataFim,
                incluiSabado = currentState.incluiSabado,
                incluiDomingo = currentState.incluiDomingo
            )

            // Soma o preço diário de todos os equipamentos multiplicado pelos dias e quantidade
            val valorTotal = itens.sumOf { item ->
                val precoUnitario = item.equipamento.getPreco(periodo) ?: 0.0
                precoUnitario * dias * item.quantidade
            }

            val valorEmCentavos = (valorTotal * 100).toLong().toString()
            _state.value = _state.value.copy(
                valorLocacao = valorEmCentavos,
                diasCalculados = dias
            )
        } else {
            // Para outros períodos, soma o preço fixo de cada equipamento multiplicado pela quantidade
            val valorTotal = itens.sumOf { item ->
                val precoUnitario = item.equipamento.getPreco(periodo) ?: 0.0
                precoUnitario * item.quantidade
            }

            val valorEmCentavos = (valorTotal * 100).toLong().toString()
            _state.value = _state.value.copy(
                valorLocacao = valorEmCentavos,
                diasCalculados = periodo.dias
            )
        }
    }
}
