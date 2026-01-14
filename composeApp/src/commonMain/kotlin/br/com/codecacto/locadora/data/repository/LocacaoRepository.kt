package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.DisponibilidadeEquipamento
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Direction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import br.com.codecacto.locadora.currentTimeMillis

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
    suspend fun getDisponibilidadeEquipamento(equipamento: Equipamento): DisponibilidadeEquipamento
    suspend fun getLocacoesAtivasList(): List<Locacao>
}

class LocacaoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : LocacaoRepository {

    private fun getUserCollection() = authRepository.currentUser?.id?.let { userId ->
        firestore.collection("usuarios").document(userId).collection(Locacao.COLLECTION_NAME)
    }

    override fun getLocacoes(): Flow<List<Locacao>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override fun getLocacoesAtivas(): Flow<List<Locacao>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .orderBy("dataFimPrevista", Direction.ASCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override fun getLocacoesFinalizadas(): Flow<List<Locacao>> {
        val collection = getUserCollection() ?: return flowOf(emptyList())

        return collection
            .where { "statusLocacao" equalTo StatusLocacao.FINALIZADA.name }
            .orderBy("criadoEm", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<Locacao>().copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun getLocacaoById(id: String): Locacao? {
        val collection = getUserCollection() ?: return null
        val doc = collection.document(id).get()
        return if (doc.exists) {
            doc.data<Locacao>().copy(id = doc.id)
        } else null
    }

    override suspend fun addLocacao(locacao: Locacao): String {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        val now = currentTimeMillis()
        val docRef = collection.add(locacao.copy(
            statusLocacao = StatusLocacao.ATIVA,
            qtdRenovacoes = 0,
            criadoEm = now,
            atualizadoEm = now
        ))
        return docRef.id
    }

    override suspend fun updateLocacao(locacao: Locacao) {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        var updatedLocacao = locacao.copy(atualizadoEm = currentTimeMillis())

        // Auto-finalizar se pagamento e coleta estiverem concluídos
        if (updatedLocacao.statusPagamento == StatusPagamento.PAGO &&
            updatedLocacao.statusColeta == StatusColeta.COLETADO) {
            updatedLocacao = updatedLocacao.copy(statusLocacao = StatusLocacao.FINALIZADA)
        }

        collection.document(locacao.id).set(updatedLocacao)
    }

    override suspend fun deleteLocacao(id: String) {
        val collection = getUserCollection()
            ?: throw Exception("Usuario nao autenticado")

        collection.document(id).delete()
    }

    override suspend fun marcarPago(id: String) {
        val collection = getUserCollection() ?: return
        val locacao = getLocacaoById(id) ?: return
        val now = currentTimeMillis()
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
        val collection = getUserCollection() ?: return
        val locacao = getLocacaoById(id) ?: return
        val now = currentTimeMillis()
        collection.document(id).set(
            locacao.copy(
                statusEntrega = StatusEntrega.ENTREGUE,
                dataEntregaReal = now,
                atualizadoEm = now
            )
        )
    }

    override suspend fun marcarColetado(id: String) {
        val collection = getUserCollection() ?: return
        val locacao = getLocacaoById(id) ?: return
        val now = currentTimeMillis()
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
        val collection = getUserCollection() ?: return
        val locacao = getLocacaoById(id) ?: return
        collection.document(id).set(
            locacao.copy(
                notaEmitida = true,
                atualizadoEm = currentTimeMillis()
            )
        )
    }

    override suspend fun renovarLocacao(id: String, novaDataFim: Long, novoValor: Double?) {
        val collection = getUserCollection() ?: return
        val locacao = getLocacaoById(id) ?: return
        val now = currentTimeMillis()
        collection.document(id).set(
            locacao.copy(
                dataFimPrevista = novaDataFim,
                valorLocacao = novoValor ?: locacao.valorLocacao,
                qtdRenovacoes = locacao.qtdRenovacoes + 1,
                ultimaRenovacaoEm = now,
                atualizadoEm = now,
                // Resetar pagamento para gerar novo recebimento
                statusPagamento = StatusPagamento.PENDENTE,
                dataPagamento = null
            )
        )
    }

    override suspend fun isEquipamentoAlugado(equipamentoId: String): Boolean {
        val collection = getUserCollection() ?: return false

        // Verifica no novo campo (equipamentoIds - lista)
        val snapshotNovo = collection
            .where { "equipamentoIds" contains equipamentoId }
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .get()

        if (snapshotNovo.documents.isNotEmpty()) {
            return true
        }

        // Fallback: verifica no campo antigo (equipamentoId - string) para dados legados
        val snapshotAntigo = collection
            .where { "equipamentoId" equalTo equipamentoId }
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .get()

        return snapshotAntigo.documents.isNotEmpty()
    }

    override suspend fun getLocacoesAtivasList(): List<Locacao> {
        val collection = getUserCollection() ?: return emptyList()

        val snapshot = collection
            .where { "statusLocacao" equalTo StatusLocacao.ATIVA.name }
            .get()

        return snapshot.documents.map { doc ->
            doc.data<Locacao>().copy(id = doc.id)
        }
    }

    override suspend fun getDisponibilidadeEquipamento(equipamento: Equipamento): DisponibilidadeEquipamento {
        val locacoesAtivas = getLocacoesAtivasList()

        // Calcula quantidade alugada e patrimônios alugados
        var quantidadeAlugada = 0
        val patrimoniosAlugadosIds = mutableSetOf<String>()

        for (locacao in locacoesAtivas) {
            val itens = locacao.getItensList()
            for (item in itens) {
                if (item.equipamentoId == equipamento.id) {
                    quantidadeAlugada += item.quantidade
                    patrimoniosAlugadosIds.addAll(item.patrimonioIds)
                }
            }
        }

        return DisponibilidadeEquipamento(
            equipamento = equipamento,
            quantidadeTotal = equipamento.quantidade,
            quantidadeAlugada = quantidadeAlugada,
            patrimoniosAlugadosIds = patrimoniosAlugadosIds
        )
    }
}
