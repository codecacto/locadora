package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.testutil.FakeLocacaoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

/**
 * Testes de integração para LocacaoRepository usando FakeLocacaoRepository.
 * Estes testes validam o comportamento esperado das operações de CRUD e lógica de negócio.
 */
class FakeLocacaoRepositoryTest {

    private val repository = FakeLocacaoRepository()

    // ==================== TESTES DE getLocacoes ====================

    @Test
    fun `getLocacoes com repositorio vazio deve retornar lista vazia`() = runTest {
        repository.clear()

        val locacoes = repository.getLocacoes().first()

        assertTrue(locacoes.isEmpty())
    }

    @Test
    fun `getLocacoes deve retornar todas as locacoes`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(id = "1", clienteId = "cli-1", equipamentoId = "equip-1")
        )
        repository.addLocacaoDirectly(
            Locacao(id = "2", clienteId = "cli-2", equipamentoId = "equip-2")
        )

        val locacoes = repository.getLocacoes().first()

        assertEquals(2, locacoes.size)
    }

    // ==================== TESTES DE getLocacoesAtivas ====================

    @Test
    fun `getLocacoesAtivas deve retornar apenas locacoes ativas`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(id = "1", clienteId = "cli-1", equipamentoId = "equip-1", statusLocacao = StatusLocacao.ATIVA)
        )
        repository.addLocacaoDirectly(
            Locacao(id = "2", clienteId = "cli-2", equipamentoId = "equip-2", statusLocacao = StatusLocacao.FINALIZADA)
        )
        repository.addLocacaoDirectly(
            Locacao(id = "3", clienteId = "cli-3", equipamentoId = "equip-3", statusLocacao = StatusLocacao.ATIVA)
        )

        val locacoesAtivas = repository.getLocacoesAtivas().first()

        assertEquals(2, locacoesAtivas.size)
        assertTrue(locacoesAtivas.all { it.statusLocacao == StatusLocacao.ATIVA })
    }

    // ==================== TESTES DE getLocacoesFinalizadas ====================

    @Test
    fun `getLocacoesFinalizadas deve retornar apenas locacoes finalizadas`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(id = "1", statusLocacao = StatusLocacao.ATIVA)
        )
        repository.addLocacaoDirectly(
            Locacao(id = "2", statusLocacao = StatusLocacao.FINALIZADA)
        )
        repository.addLocacaoDirectly(
            Locacao(id = "3", statusLocacao = StatusLocacao.FINALIZADA)
        )

        val locacoesFinalizadas = repository.getLocacoesFinalizadas().first()

        assertEquals(2, locacoesFinalizadas.size)
        assertTrue(locacoesFinalizadas.all { it.statusLocacao == StatusLocacao.FINALIZADA })
    }

    // ==================== TESTES DE addLocacao ====================

    @Test
    fun `addLocacao deve criar locacao com status ATIVA`() = runTest {
        repository.clear()
        val novaLocacao = Locacao(
            clienteId = "cli-1",
            equipamentoId = "equip-1",
            valorLocacao = 500.0,
            periodo = PeriodoLocacao.MENSAL
        )

        val id = repository.addLocacao(novaLocacao)

        val locacaoSalva = repository.getLocacaoById(id)
        assertNotNull(locacaoSalva)
        assertEquals(StatusLocacao.ATIVA, locacaoSalva.statusLocacao)
        assertEquals(500.0, locacaoSalva.valorLocacao)
    }

    // ==================== TESTES DE marcarPago ====================

    @Test
    fun `marcarPago deve atualizar status de pagamento`() = runTest {
        repository.clear()
        val id = repository.addLocacao(
            Locacao(clienteId = "cli-1", equipamentoId = "equip-1", statusPagamento = StatusPagamento.PENDENTE)
        )

        repository.marcarPago(id)

        val locacao = repository.getLocacaoById(id)
        assertNotNull(locacao)
        assertEquals(StatusPagamento.PAGO, locacao.statusPagamento)
        assertNotNull(locacao.dataPagamento)
    }

    @Test
    fun `marcarPago com coleta realizada deve auto-finalizar`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(
                id = "loc-1",
                statusPagamento = StatusPagamento.PENDENTE,
                statusColeta = StatusColeta.COLETADO,
                statusLocacao = StatusLocacao.ATIVA
            )
        )

        repository.marcarPago("loc-1")

        val locacao = repository.getLocacaoById("loc-1")
        assertNotNull(locacao)
        assertEquals(StatusPagamento.PAGO, locacao.statusPagamento)
        assertEquals(StatusLocacao.FINALIZADA, locacao.statusLocacao)
    }

    // ==================== TESTES DE marcarEntregue ====================

    @Test
    fun `marcarEntregue deve atualizar status de entrega`() = runTest {
        repository.clear()
        val id = repository.addLocacao(
            Locacao(clienteId = "cli-1", equipamentoId = "equip-1", statusEntrega = StatusEntrega.AGENDADA)
        )

        repository.marcarEntregue(id)

        val locacao = repository.getLocacaoById(id)
        assertNotNull(locacao)
        assertEquals(StatusEntrega.ENTREGUE, locacao.statusEntrega)
        assertNotNull(locacao.dataEntregaReal)
    }

    // ==================== TESTES DE marcarColetado ====================

    @Test
    fun `marcarColetado deve atualizar status de coleta`() = runTest {
        repository.clear()
        val id = repository.addLocacao(
            Locacao(clienteId = "cli-1", equipamentoId = "equip-1", statusColeta = StatusColeta.NAO_COLETADO)
        )

        repository.marcarColetado(id)

        val locacao = repository.getLocacaoById(id)
        assertNotNull(locacao)
        assertEquals(StatusColeta.COLETADO, locacao.statusColeta)
        assertNotNull(locacao.dataColeta)
    }

    @Test
    fun `marcarColetado com pagamento realizado deve auto-finalizar`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(
                id = "loc-1",
                statusPagamento = StatusPagamento.PAGO,
                statusColeta = StatusColeta.NAO_COLETADO,
                statusLocacao = StatusLocacao.ATIVA
            )
        )

        repository.marcarColetado("loc-1")

        val locacao = repository.getLocacaoById("loc-1")
        assertNotNull(locacao)
        assertEquals(StatusColeta.COLETADO, locacao.statusColeta)
        assertEquals(StatusLocacao.FINALIZADA, locacao.statusLocacao)
    }

    // ==================== TESTES DE renovarLocacao ====================

    @Test
    fun `renovarLocacao deve incrementar contador e atualizar data`() = runTest {
        repository.clear()
        val now = System.currentTimeMillis()
        val novaDataFim = now + (30 * 24 * 60 * 60 * 1000L) // +30 dias
        repository.addLocacaoDirectly(
            Locacao(
                id = "loc-1",
                clienteId = "cli-1",
                equipamentoId = "equip-1",
                valorLocacao = 500.0,
                qtdRenovacoes = 0,
                statusPagamento = StatusPagamento.PAGO
            )
        )

        repository.renovarLocacao("loc-1", novaDataFim, 600.0)

        val locacao = repository.getLocacaoById("loc-1")
        assertNotNull(locacao)
        assertEquals(1, locacao.qtdRenovacoes)
        assertEquals(600.0, locacao.valorLocacao)
        assertEquals(novaDataFim, locacao.dataFimPrevista)
        assertEquals(StatusPagamento.PENDENTE, locacao.statusPagamento) // Resetado
        assertNotNull(locacao.ultimaRenovacaoEm)
    }

    @Test
    fun `renovarLocacao sem novo valor deve manter valor anterior`() = runTest {
        repository.clear()
        val novaDataFim = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
        repository.addLocacaoDirectly(
            Locacao(id = "loc-1", valorLocacao = 500.0, qtdRenovacoes = 0)
        )

        repository.renovarLocacao("loc-1", novaDataFim, null)

        val locacao = repository.getLocacaoById("loc-1")
        assertNotNull(locacao)
        assertEquals(500.0, locacao.valorLocacao)
    }

    // ==================== TESTES DE isEquipamentoAlugado ====================

    @Test
    fun `isEquipamentoAlugado com equipamento em locacao ativa deve retornar true`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(
                id = "loc-1",
                equipamentoId = "equip-123",
                statusLocacao = StatusLocacao.ATIVA
            )
        )

        val isAlugado = repository.isEquipamentoAlugado("equip-123")

        assertTrue(isAlugado)
    }

    @Test
    fun `isEquipamentoAlugado com equipamento em locacao finalizada deve retornar false`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(
                id = "loc-1",
                equipamentoId = "equip-123",
                statusLocacao = StatusLocacao.FINALIZADA
            )
        )

        val isAlugado = repository.isEquipamentoAlugado("equip-123")

        assertFalse(isAlugado)
    }

    @Test
    fun `isEquipamentoAlugado com equipamento sem locacao deve retornar false`() = runTest {
        repository.clear()

        val isAlugado = repository.isEquipamentoAlugado("equip-inexistente")

        assertFalse(isAlugado)
    }

    // ==================== TESTES DE marcarNotaEmitida ====================

    @Test
    fun `marcarNotaEmitida deve atualizar flag de nota emitida`() = runTest {
        repository.clear()
        repository.addLocacaoDirectly(
            Locacao(id = "loc-1", emitirNota = true, notaEmitida = false)
        )

        repository.marcarNotaEmitida("loc-1")

        val locacao = repository.getLocacaoById("loc-1")
        assertNotNull(locacao)
        assertTrue(locacao.notaEmitida)
    }

    // ==================== TESTES DE deleteLocacao ====================

    @Test
    fun `deleteLocacao deve remover locacao`() = runTest {
        repository.clear()
        val id = repository.addLocacao(
            Locacao(clienteId = "cli-1", equipamentoId = "equip-1")
        )

        repository.deleteLocacao(id)

        val locacaoDeletada = repository.getLocacaoById(id)
        assertNull(locacaoDeletada)
    }

    // ==================== TESTES DE FLUXO COMPLETO ====================

    @Test
    fun `fluxo completo de locacao - criar, entregar, pagar, coletar, finalizar`() = runTest {
        repository.clear()

        // 1. Criar locação
        val id = repository.addLocacao(
            Locacao(
                clienteId = "cli-1",
                equipamentoId = "equip-1",
                valorLocacao = 500.0,
                periodo = PeriodoLocacao.MENSAL
            )
        )

        var locacao = repository.getLocacaoById(id)!!
        assertEquals(StatusLocacao.ATIVA, locacao.statusLocacao)
        assertEquals(StatusEntrega.NAO_AGENDADA, locacao.statusEntrega)
        assertEquals(StatusPagamento.PENDENTE, locacao.statusPagamento)
        assertEquals(StatusColeta.NAO_COLETADO, locacao.statusColeta)

        // 2. Marcar como entregue
        repository.marcarEntregue(id)
        locacao = repository.getLocacaoById(id)!!
        assertEquals(StatusEntrega.ENTREGUE, locacao.statusEntrega)
        assertEquals(StatusLocacao.ATIVA, locacao.statusLocacao) // Ainda ativa

        // 3. Marcar como pago
        repository.marcarPago(id)
        locacao = repository.getLocacaoById(id)!!
        assertEquals(StatusPagamento.PAGO, locacao.statusPagamento)
        assertEquals(StatusLocacao.ATIVA, locacao.statusLocacao) // Ainda ativa (falta coleta)

        // 4. Marcar como coletado - deve auto-finalizar
        repository.marcarColetado(id)
        locacao = repository.getLocacaoById(id)!!
        assertEquals(StatusColeta.COLETADO, locacao.statusColeta)
        assertEquals(StatusLocacao.FINALIZADA, locacao.statusLocacao) // Agora finalizada!
    }
}
