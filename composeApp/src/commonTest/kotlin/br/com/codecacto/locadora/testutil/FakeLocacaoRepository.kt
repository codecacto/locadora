package br.com.codecacto.locadora.testutil

import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake LocacaoRepository para testes.
 */
class FakeLocacaoRepository : LocacaoRepository {

    private val locacoesFlow = MutableStateFlow<List<Locacao>>(emptyList())
    private var nextId = 1

    var shouldThrowOnAdd = false
    var shouldThrowOnUpdate = false
    var shouldThrowOnDelete = false
    var exceptionToThrow: Exception = Exception("Erro simulado")

    override fun getLocacoes(): Flow<List<Locacao>> = locacoesFlow

    override fun getLocacoesAtivas(): Flow<List<Locacao>> = locacoesFlow.map { locacoes ->
        locacoes.filter { it.statusLocacao == StatusLocacao.ATIVA }
    }

    override fun getLocacoesFinalizadas(): Flow<List<Locacao>> = locacoesFlow.map { locacoes ->
        locacoes.filter { it.statusLocacao == StatusLocacao.FINALIZADA }
    }

    override suspend fun getLocacaoById(id: String): Locacao? {
        return locacoesFlow.value.find { it.id == id }
    }

    override suspend fun addLocacao(locacao: Locacao): String {
        if (shouldThrowOnAdd) throw exceptionToThrow

        val id = "loc-${nextId++}"
        val newLocacao = locacao.copy(id = id, statusLocacao = StatusLocacao.ATIVA)
        locacoesFlow.value = locacoesFlow.value + newLocacao
        return id
    }

    override suspend fun updateLocacao(locacao: Locacao) {
        if (shouldThrowOnUpdate) throw exceptionToThrow

        var updatedLocacao = locacao.copy(atualizadoEm = System.currentTimeMillis())

        // Auto-finalizar se pagamento e coleta estiverem concluídos
        if (updatedLocacao.statusPagamento == StatusPagamento.PAGO &&
            updatedLocacao.statusColeta == StatusColeta.COLETADO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == locacao.id) updatedLocacao else it
        }
    }

    override suspend fun deleteLocacao(id: String) {
        if (shouldThrowOnDelete) throw exceptionToThrow

        locacoesFlow.value = locacoesFlow.value.filter { it.id != id }
    }

    override suspend fun marcarPago(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        var updatedLocacao = locacao.copy(
            statusPagamento = StatusPagamento.PAGO,
            dataPagamento = now,
            atualizadoEm = now
        )

        if (updatedLocacao.statusColeta == StatusColeta.COLETADO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == id) updatedLocacao else it
        }
    }

    override suspend fun marcarEntregue(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        val updatedLocacao = locacao.copy(
            statusEntrega = StatusEntrega.ENTREGUE,
            dataEntregaReal = now,
            atualizadoEm = now
        )

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == id) updatedLocacao else it
        }
    }

    override suspend fun marcarColetado(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        var updatedLocacao = locacao.copy(
            statusColeta = StatusColeta.COLETADO,
            dataColeta = now,
            atualizadoEm = now
        )

        if (updatedLocacao.statusPagamento == StatusPagamento.PAGO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == id) updatedLocacao else it
        }
    }

    override suspend fun marcarNotaEmitida(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val updatedLocacao = locacao.copy(
            notaEmitida = true,
            atualizadoEm = System.currentTimeMillis()
        )

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == id) updatedLocacao else it
        }
    }

    override suspend fun renovarLocacao(id: String, novaDataFim: Long, novoValor: Double?) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        val updatedLocacao = locacao.copy(
            dataFimPrevista = novaDataFim,
            valorLocacao = novoValor ?: locacao.valorLocacao,
            qtdRenovacoes = locacao.qtdRenovacoes + 1,
            ultimaRenovacaoEm = now,
            atualizadoEm = now,
            statusPagamento = StatusPagamento.PENDENTE,
            dataPagamento = null
        )

        locacoesFlow.value = locacoesFlow.value.map {
            if (it.id == id) updatedLocacao else it
        }
    }

    override suspend fun isEquipamentoAlugado(equipamentoId: String): Boolean {
        return locacoesFlow.value.any {
            it.equipamentoId == equipamentoId && it.statusLocacao == StatusLocacao.ATIVA
        }
    }

    // Helpers para testes
    fun setLocacoes(locacoes: List<Locacao>) {
        locacoesFlow.value = locacoes
    }

    fun addLocacaoDirectly(locacao: Locacao) {
        locacoesFlow.value = locacoesFlow.value + locacao
    }

    fun clear() {
        locacoesFlow.value = emptyList()
        nextId = 1
    }
}
