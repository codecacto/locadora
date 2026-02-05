package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.core.pdf.RecebimentoReceiptData
import br.com.codecacto.locadora.core.pdf.ReceiptPdfGenerator
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RecebimentosViewModel(
    private val recebimentoRepository: RecebimentoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository,
    private val locacaoRepository: LocacaoRepository,
    private val dadosEmpresaRepository: DadosEmpresaRepository,
    private val receiptPdfGenerator: ReceiptPdfGenerator,
    errorHandler: ErrorHandler
) : BaseViewModel<RecebimentosContract.State, RecebimentosContract.Effect, RecebimentosContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(RecebimentosContract.State())
    override val state: StateFlow<RecebimentosContract.State> = _state.asStateFlow()

    init {
        loadRecebimentos()
    }

    override fun onAction(action: RecebimentosContract.Action) {
        when (action) {
            is RecebimentosContract.Action.SelectTab -> {
                _state.value = _state.value.copy(tabSelecionada = action.tab)
            }
            is RecebimentosContract.Action.MarcarRecebido -> marcarRecebido(action.recebimentoId)
            is RecebimentosContract.Action.DeleteRecebimento -> deleteRecebimento(action.recebimentoId)
            is RecebimentosContract.Action.SelectRecebimento -> {
                emitEffect(RecebimentosContract.Effect.NavigateToDetalhes(action.recebimento.locacaoId))
            }
            is RecebimentosContract.Action.SelectMes -> selectMes(action.mesAno)
            is RecebimentosContract.Action.GerarRecibo -> gerarRecibo(action.recebimento)
            is RecebimentosContract.Action.Refresh -> refreshRecebimentos()
        }
    }

    private fun selectMes(mesAno: MesAno?) {
        val currentState = _state.value
        val (pendentesFiltrados, pagosFiltrados) = filtrarPorMes(
            currentState.recebimentosPendentes,
            currentState.recebimentosPagos,
            mesAno
        )
        val totalPendente = pendentesFiltrados.sumOf { it.recebimento.valor }
        val totalPago = pagosFiltrados.sumOf { it.recebimento.valor }

        _state.value = currentState.copy(
            mesSelecionado = mesAno,
            recebimentosPendentesFiltrados = pendentesFiltrados,
            recebimentosPagosFiltrados = pagosFiltrados,
            totalPendente = totalPendente,
            totalPago = totalPago
        )
    }

    private fun filtrarPorMes(
        pendentes: List<RecebimentoComDetalhes>,
        pagos: List<RecebimentoComDetalhes>,
        mesAno: MesAno?
    ): Pair<List<RecebimentoComDetalhes>, List<RecebimentoComDetalhes>> {
        if (mesAno == null) {
            return Pair(pendentes, pagos)
        }

        val pendentesFiltrados = pendentes.filter { recebimento ->
            val localDate = Instant.fromEpochMilliseconds(recebimento.recebimento.dataVencimento)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            localDate.monthNumber == mesAno.mes && localDate.year == mesAno.ano
        }

        val pagosFiltrados = pagos.filter { recebimento ->
            val timestamp = recebimento.recebimento.dataPagamento ?: recebimento.recebimento.dataVencimento
            val localDate = Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            localDate.monthNumber == mesAno.mes && localDate.year == mesAno.ano
        }

        return Pair(pendentesFiltrados, pagosFiltrados)
    }

    private fun calcularMesesDisponiveis(
        pendentes: List<RecebimentoComDetalhes>,
        pagos: List<RecebimentoComDetalhes>
    ): List<MesAno> {
        val meses = mutableSetOf<MesAno>()

        pendentes.forEach { recebimento ->
            val localDate = Instant.fromEpochMilliseconds(recebimento.recebimento.dataVencimento)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            meses.add(MesAno(localDate.monthNumber, localDate.year))
        }

        pagos.forEach { recebimento ->
            val timestamp = recebimento.recebimento.dataPagamento ?: recebimento.recebimento.dataVencimento
            val localDate = Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            meses.add(MesAno(localDate.monthNumber, localDate.year))
        }

        return meses.sortedByDescending { it.ano * 100 + it.mes }
    }

    private fun refreshRecebimentos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadRecebimentos(isRefresh = true)
        }
    }

    private fun loadRecebimentos(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) {
                _state.value = _state.value.copy(isLoading = true)
            }

            combine(
                recebimentoRepository.getRecebimentosPendentes(),
                recebimentoRepository.getRecebimentosPagos(),
                clienteRepository.getClientes(),
                equipamentoRepository.getEquipamentos()
            ) { pendentes, pagos, clientes, equipamentos ->
                val clientesMap = clientes.associateBy { it.id }
                val equipamentosMap = equipamentos.associateBy { it.id }

                val recebimentosPendentes = pendentes.map { recebimento ->
                    val equipamentosDoRecebimento = recebimento.getEquipamentoIdsList()
                        .mapNotNull { equipamentosMap[it] }
                    RecebimentoComDetalhes(
                        recebimento = recebimento,
                        cliente = clientesMap[recebimento.clienteId],
                        equipamentos = equipamentosDoRecebimento
                    )
                }

                val recebimentosPagos = pagos.map { recebimento ->
                    val equipamentosDoRecebimento = recebimento.getEquipamentoIdsList()
                        .mapNotNull { equipamentosMap[it] }
                    RecebimentoComDetalhes(
                        recebimento = recebimento,
                        cliente = clientesMap[recebimento.clienteId],
                        equipamentos = equipamentosDoRecebimento
                    )
                }

                Pair(recebimentosPendentes, recebimentosPagos)
            }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao carregar recebimentos"
                    )
                    handleError(e)
                }
                .collect { (pendentes, pagos) ->
                    val mesesDisponiveis = calcularMesesDisponiveis(pendentes, pagos)
                    val mesSelecionado = _state.value.mesSelecionado

                    val (pendentesFiltrados, pagosFiltrados) = filtrarPorMes(pendentes, pagos, mesSelecionado)
                    val totalPendente = pendentesFiltrados.sumOf { it.recebimento.valor }
                    val totalPago = pagosFiltrados.sumOf { it.recebimento.valor }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        recebimentosPendentes = pendentes,
                        recebimentosPagos = pagos,
                        recebimentosPendentesFiltrados = pendentesFiltrados,
                        recebimentosPagosFiltrados = pagosFiltrados,
                        totalPendente = totalPendente,
                        totalPago = totalPago,
                        mesesDisponiveis = mesesDisponiveis,
                        error = null
                    )
                }
        }
    }

    private fun marcarRecebido(recebimentoId: String) {
        viewModelScope.launch {
            try {
                recebimentoRepository.marcarPago(recebimentoId)
                emitEffect(RecebimentosContract.Effect.ShowSuccess("Pagamento confirmado!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(RecebimentosContract.Effect.ShowError(e.message ?: "Erro ao confirmar recebimento"))
            }
        }
    }

    private fun deleteRecebimento(recebimentoId: String) {
        viewModelScope.launch {
            try {
                recebimentoRepository.deleteRecebimento(recebimentoId)
                emitEffect(RecebimentosContract.Effect.ShowSuccess("Recebimento excluído!"))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(RecebimentosContract.Effect.ShowError(e.message ?: "Erro ao excluir recebimento"))
            }
        }
    }

    private fun gerarRecibo(recebimentoComDetalhes: RecebimentoComDetalhes) {
        viewModelScope.launch {
            try {
                val dadosEmpresa = dadosEmpresaRepository.getDadosEmpresa().first()

                // Verifica se os dados da empresa estão preenchidos
                if (dadosEmpresa.nomeEmpresa.isBlank() || dadosEmpresa.documento.isBlank()) {
                    emitEffect(RecebimentosContract.Effect.DadosEmpresaNaoPreenchidos)
                    return@launch
                }

                val cliente = recebimentoComDetalhes.cliente
                if (cliente == null) {
                    emitEffect(RecebimentosContract.Effect.ShowError("Dados do cliente não encontrados"))
                    return@launch
                }

                // Busca a locação para obter mais detalhes se necessário
                val locacao = locacaoRepository.getLocacaoById(recebimentoComDetalhes.recebimento.locacaoId)

                val receiptData = RecebimentoReceiptData(
                    recebimento = recebimentoComDetalhes.recebimento,
                    cliente = cliente,
                    equipamentos = recebimentoComDetalhes.equipamentos,
                    dadosEmpresa = dadosEmpresa,
                    locacao = locacao
                )

                val filePath = receiptPdfGenerator.generateRecebimentoReceipt(receiptData)
                emitEffect(RecebimentosContract.Effect.CompartilharRecibo(filePath))
            } catch (e: Exception) {
                handleError(e)
                emitEffect(RecebimentosContract.Effect.ShowError(e.message ?: "Erro ao gerar recibo"))
            }
        }
    }
}
