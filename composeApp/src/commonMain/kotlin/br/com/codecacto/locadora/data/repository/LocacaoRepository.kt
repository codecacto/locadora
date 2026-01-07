package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusEntrega
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocacaoRepository {
    fun getLocacoes(): Flow<List<Locacao>>
    fun getLocacoesAtivas(): Flow<List<Locacao>>
    fun getLocacoesFinalizadas(): Flow<List<Locacao>>
    suspend fun getLocacaoById(id: String): Locacao?
    suspend fun addLocacao(locacao: Locacao): String
    suspend fun updateLocacao(locacao: Locacao)
    suspend fun deleteLocacao(id: String)
    suspend fun marcarPago(id: String)
    suspend fun marcarEntregue(id: String)
    suspend fun marcarColetado(id: String)
    suspend fun marcarNotaEmitida(id: String)
    suspend fun renovarLocacao(id: String, novaDataFim: Long, novoValor: Double?)
    suspend fun isEquipamentoAlugado(equipamentoId: String): Boolean
}

class LocacaoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : LocacaoRepository {

    private val collection = firestore.collection(Locacao.COLLECTION_NAME)

    override fun getLocacoes(): Flow<List<Locacao>> {
        return collection
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
    }

    override fun getLocacoesAtivas(): Flow<List<Locacao>> {
        return collection
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .orderBy("dataFimPrevista", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
    }

    override fun getLocacoesFinalizadas(): Flow<List<Locacao>> {
        return collection
            .where { "statusLocacao" equalTo StatusLocacao.FINALIZADA.name }
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
    }

    override suspend fun getLocacaoById(id: String): Locacao? {
        return try {
            val doc = collection.document(id).get()
            if (doc.exists) {
                doc.data<Locacao>().copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addLocacao(locacao: Locacao): String {
        val now = System.currentTimeMillis()
        val docRef = collection.add(locacao.copy(
            statusLocacao = StatusLocacao.ATIVA,
            qtdRenovacoes = 0,
            criadoEm = now,
            atualizadoEm = now
        ))
        return docRef.id
    }

    override suspend fun updateLocacao(locacao: Locacao) {
        var updatedLocacao = locacao.copy(atualizadoEm = System.currentTimeMillis())

        // Auto-finalizar se pagamento e coleta estiverem concluídos
        if (updatedLocacao.statusPagamento == StatusPagamento.PAGO &&
            updatedLocacao.statusColeta == StatusColeta.COLETADO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        collection.document(locacao.id).set(updatedLocacao)
    }

    override suspend fun deleteLocacao(id: String) {
        collection.document(id).delete()
    }

    override suspend fun marcarPago(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        var updatedLocacao = locacao.copy(
            statusPagamento = StatusPagamento.PAGO,
            dataPagamento = now,
            atualizadoEm = now
        )

        // Auto-finalizar se coleta já foi feita
        if (updatedLocacao.statusColeta == StatusColeta.COLETADO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        collection.document(id).set(updatedLocacao)
    }

    override suspend fun marcarEntregue(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        collection.document(id).set(
            locacao.copy(
                statusEntrega = StatusEntrega.ENTREGUE,
                dataEntregaReal = now,
                atualizadoEm = now
            )
        )
    }

    override suspend fun marcarColetado(id: String) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        var updatedLocacao = locacao.copy(
            statusColeta = StatusColeta.COLETADO,
            dataColeta = now,
            atualizadoEm = now
        )

        // Auto-finalizar se pagamento já foi feito
        if (updatedLocacao.statusPagamento == StatusPagamento.PAGO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        collection.document(id).set(updatedLocacao)
    }

    override suspend fun marcarNotaEmitida(id: String) {
        val locacao = getLocacaoById(id) ?: return
        collection.document(id).set(
            locacao.copy(
                notaEmitida = true,
                atualizadoEm = System.currentTimeMillis()
            )
        )
    }

    override suspend fun renovarLocacao(id: String, novaDataFim: Long, novoValor: Double?) {
        val locacao = getLocacaoById(id) ?: return
        val now = System.currentTimeMillis()
        collection.document(id).set(
            locacao.copy(
                dataFimPrevista = novaDataFim,
                valorLocacao = novoValor ?: locacao.valorLocacao,
                qtdRenovacoes = locacao.qtdRenovacoes + 1,
                ultimaRenovacaoEm = now,
                atualizadoEm = now
            )
        )
    }

    override suspend fun isEquipamentoAlugado(equipamentoId: String): Boolean {
        val snapshot = collection
            .where { "equipamentoId" equalTo equipamentoId }
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .get()
        return snapshot.documents.isNotEmpty()
    }
}
