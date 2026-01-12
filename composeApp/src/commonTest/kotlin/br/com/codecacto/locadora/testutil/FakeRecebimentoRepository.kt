package br.com.codecacto.locadora.testutil

import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.data.repository.RecebimentoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementação fake do RecebimentoRepository para testes.
 * Mantém os recebimentos em memória e oferece controle completo para cenários de teste.
 */
class FakeRecebimentoRepository : RecebimentoRepository {

    private val _recebimentos = MutableStateFlow<List<Recebimento>>(emptyList())
    private var idCounter = 0

    var shouldThrowError = false
    var errorMessage = "Erro simulado"

    /**
     * Adiciona recebimentos para configuração de testes.
     */
    fun setRecebimentos(recebimentos: List<Recebimento>) {
        _recebimentos.value = recebimentos
    }

    /**
     * Limpa todos os recebimentos.
     */
    fun clear() {
        _recebimentos.value = emptyList()
        idCounter = 0
    }

    override fun getRecebimentos(): Flow<List<Recebimento>> {
        return _recebimentos.map { list ->
            list.sortedBy { it.dataVencimento }
        }
    }

    override fun getRecebimentosPendentes(): Flow<List<Recebimento>> {
        return _recebimentos.map { list ->
            list.filter { it.status == StatusPagamento.PENDENTE }
                .sortedBy { it.dataVencimento }
        }
    }

    override fun getRecebimentosPagos(): Flow<List<Recebimento>> {
        return _recebimentos.map { list ->
            list.filter { it.status == StatusPagamento.PAGO }
                .sortedByDescending { it.dataPagamento ?: it.criadoEm }
        }
    }

    override fun getRecebimentosByLocacao(locacaoId: String): Flow<List<Recebimento>> {
        return _recebimentos.map { list ->
            list.filter { it.locacaoId == locacaoId }
                .sortedBy { it.numeroRenovacao }
        }
    }

    override suspend fun addRecebimento(recebimento: Recebimento): String {
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }

        val newId = "rec-${++idCounter}"
        val novoRecebimento = recebimento.copy(
            id = newId,
            criadoEm = System.currentTimeMillis()
        )

        _recebimentos.value = _recebimentos.value + novoRecebimento
        return newId
    }

    override suspend fun marcarPago(recebimentoId: String) {
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }

        _recebimentos.value = _recebimentos.value.map { recebimento ->
            if (recebimento.id == recebimentoId) {
                recebimento.copy(
                    status = StatusPagamento.PAGO,
                    dataPagamento = System.currentTimeMillis()
                )
            } else {
                recebimento
            }
        }
    }

    override suspend fun deleteRecebimentosByLocacao(locacaoId: String) {
        if (shouldThrowError) {
            throw Exception(errorMessage)
        }

        _recebimentos.value = _recebimentos.value.filter { it.locacaoId != locacaoId }
    }
}
