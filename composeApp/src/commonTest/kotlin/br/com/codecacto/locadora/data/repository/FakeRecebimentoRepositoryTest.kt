package br.com.codecacto.locadora.data.repository

import br.com.codecacto.locadora.core.model.Recebimento
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.testutil.FakeRecebimentoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith

/**
 * Testes unitários para FakeRecebimentoRepository.
 */
class FakeRecebimentoRepositoryTest {

    private lateinit var repository: FakeRecebimentoRepository

    @BeforeTest
    fun setup() {
        repository = FakeRecebimentoRepository()
        repository.clear()
    }

    // ==================== TESTES DE getRecebimentos ====================

    @Test
    fun `getRecebimentos - lista vazia inicialmente`() = runTest {
        val recebimentos = repository.getRecebimentos().first()
        assertTrue(recebimentos.isEmpty())
    }

    @Test
    fun `getRecebimentos - retorna todos ordenados por data vencimento`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", dataVencimento = 3000L),
            Recebimento(id = "rec-2", dataVencimento = 1000L),
            Recebimento(id = "rec-3", dataVencimento = 2000L)
        )
        repository.setRecebimentos(recebimentos)

        val resultado = repository.getRecebimentos().first()

        assertEquals(3, resultado.size)
        assertEquals("rec-2", resultado[0].id) // 1000L primeiro
        assertEquals("rec-3", resultado[1].id) // 2000L segundo
        assertEquals("rec-1", resultado[2].id) // 3000L terceiro
    }

    // ==================== TESTES DE getRecebimentosPendentes ====================

    @Test
    fun `getRecebimentosPendentes - retorna apenas pendentes`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PENDENTE, dataVencimento = 2000L),
            Recebimento(id = "rec-2", status = StatusPagamento.PAGO, dataVencimento = 1000L),
            Recebimento(id = "rec-3", status = StatusPagamento.PENDENTE, dataVencimento = 3000L)
        )
        repository.setRecebimentos(recebimentos)

        val pendentes = repository.getRecebimentosPendentes().first()

        assertEquals(2, pendentes.size)
        assertTrue(pendentes.all { it.status == StatusPagamento.PENDENTE })
    }

    @Test
    fun `getRecebimentosPendentes - ordenados por data vencimento`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PENDENTE, dataVencimento = 3000L),
            Recebimento(id = "rec-2", status = StatusPagamento.PENDENTE, dataVencimento = 1000L)
        )
        repository.setRecebimentos(recebimentos)

        val pendentes = repository.getRecebimentosPendentes().first()

        assertEquals("rec-2", pendentes[0].id) // Vence primeiro
        assertEquals("rec-1", pendentes[1].id)
    }

    @Test
    fun `getRecebimentosPendentes - lista vazia quando nao ha pendentes`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PAGO),
            Recebimento(id = "rec-2", status = StatusPagamento.PAGO)
        )
        repository.setRecebimentos(recebimentos)

        val pendentes = repository.getRecebimentosPendentes().first()

        assertTrue(pendentes.isEmpty())
    }

    // ==================== TESTES DE getRecebimentosPagos ====================

    @Test
    fun `getRecebimentosPagos - retorna apenas pagos`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PENDENTE),
            Recebimento(id = "rec-2", status = StatusPagamento.PAGO, dataPagamento = 1000L),
            Recebimento(id = "rec-3", status = StatusPagamento.PAGO, dataPagamento = 2000L)
        )
        repository.setRecebimentos(recebimentos)

        val pagos = repository.getRecebimentosPagos().first()

        assertEquals(2, pagos.size)
        assertTrue(pagos.all { it.status == StatusPagamento.PAGO })
    }

    @Test
    fun `getRecebimentosPagos - ordenados por data pagamento decrescente`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PAGO, dataPagamento = 1000L),
            Recebimento(id = "rec-2", status = StatusPagamento.PAGO, dataPagamento = 3000L),
            Recebimento(id = "rec-3", status = StatusPagamento.PAGO, dataPagamento = 2000L)
        )
        repository.setRecebimentos(recebimentos)

        val pagos = repository.getRecebimentosPagos().first()

        assertEquals("rec-2", pagos[0].id) // 3000L - mais recente
        assertEquals("rec-3", pagos[1].id) // 2000L
        assertEquals("rec-1", pagos[2].id) // 1000L - mais antigo
    }

    // ==================== TESTES DE getRecebimentosByLocacao ====================

    @Test
    fun `getRecebimentosByLocacao - filtra por locacao`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123", numeroRenovacao = 0),
            Recebimento(id = "rec-2", locacaoId = "loc-456", numeroRenovacao = 0),
            Recebimento(id = "rec-3", locacaoId = "loc-123", numeroRenovacao = 1)
        )
        repository.setRecebimentos(recebimentos)

        val resultado = repository.getRecebimentosByLocacao("loc-123").first()

        assertEquals(2, resultado.size)
        assertTrue(resultado.all { it.locacaoId == "loc-123" })
    }

    @Test
    fun `getRecebimentosByLocacao - ordenados por numero renovacao`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123", numeroRenovacao = 2),
            Recebimento(id = "rec-2", locacaoId = "loc-123", numeroRenovacao = 0),
            Recebimento(id = "rec-3", locacaoId = "loc-123", numeroRenovacao = 1)
        )
        repository.setRecebimentos(recebimentos)

        val resultado = repository.getRecebimentosByLocacao("loc-123").first()

        assertEquals("rec-2", resultado[0].id) // Renovacao 0
        assertEquals("rec-3", resultado[1].id) // Renovacao 1
        assertEquals("rec-1", resultado[2].id) // Renovacao 2
    }

    @Test
    fun `getRecebimentosByLocacao - retorna vazio para locacao inexistente`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123")
        )
        repository.setRecebimentos(recebimentos)

        val resultado = repository.getRecebimentosByLocacao("loc-inexistente").first()

        assertTrue(resultado.isEmpty())
    }

    // ==================== TESTES DE addRecebimento ====================

    @Test
    fun `addRecebimento - adiciona novo recebimento`() = runTest {
        val recebimento = Recebimento(
            locacaoId = "loc-123",
            clienteId = "cli-456",
            equipamentoId = "eq-789",
            valor = 500.0,
            status = StatusPagamento.PENDENTE
        )

        val id = repository.addRecebimento(recebimento)

        assertNotNull(id)
        assertTrue(id.startsWith("rec-"))

        val recebimentos = repository.getRecebimentos().first()
        assertEquals(1, recebimentos.size)
        assertEquals("loc-123", recebimentos[0].locacaoId)
        assertEquals(500.0, recebimentos[0].valor)
    }

    @Test
    fun `addRecebimento - gera id unico`() = runTest {
        val recebimento1 = Recebimento(locacaoId = "loc-1")
        val recebimento2 = Recebimento(locacaoId = "loc-2")

        val id1 = repository.addRecebimento(recebimento1)
        val id2 = repository.addRecebimento(recebimento2)

        assertTrue(id1 != id2)
        assertEquals("rec-1", id1)
        assertEquals("rec-2", id2)
    }

    @Test
    fun `addRecebimento - lanca erro quando configurado`() = runTest {
        repository.shouldThrowError = true
        repository.errorMessage = "Erro ao adicionar"

        assertFailsWith<Exception> {
            repository.addRecebimento(Recebimento())
        }
    }

    // ==================== TESTES DE marcarPago ====================

    @Test
    fun `marcarPago - atualiza status para PAGO`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PENDENTE)
        )
        repository.setRecebimentos(recebimentos)

        repository.marcarPago("rec-1")

        val resultado = repository.getRecebimentos().first()
        assertEquals(StatusPagamento.PAGO, resultado[0].status)
        assertNotNull(resultado[0].dataPagamento)
    }

    @Test
    fun `marcarPago - nao afeta outros recebimentos`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", status = StatusPagamento.PENDENTE),
            Recebimento(id = "rec-2", status = StatusPagamento.PENDENTE)
        )
        repository.setRecebimentos(recebimentos)

        repository.marcarPago("rec-1")

        val resultado = repository.getRecebimentos().first()
        val rec1 = resultado.find { it.id == "rec-1" }
        val rec2 = resultado.find { it.id == "rec-2" }

        assertEquals(StatusPagamento.PAGO, rec1?.status)
        assertEquals(StatusPagamento.PENDENTE, rec2?.status)
    }

    @Test
    fun `marcarPago - lanca erro quando configurado`() = runTest {
        repository.setRecebimentos(listOf(Recebimento(id = "rec-1")))
        repository.shouldThrowError = true

        assertFailsWith<Exception> {
            repository.marcarPago("rec-1")
        }
    }

    // ==================== TESTES DE deleteRecebimentosByLocacao ====================

    @Test
    fun `deleteRecebimentosByLocacao - remove todos da locacao`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123"),
            Recebimento(id = "rec-2", locacaoId = "loc-123"),
            Recebimento(id = "rec-3", locacaoId = "loc-456")
        )
        repository.setRecebimentos(recebimentos)

        repository.deleteRecebimentosByLocacao("loc-123")

        val resultado = repository.getRecebimentos().first()
        assertEquals(1, resultado.size)
        assertEquals("rec-3", resultado[0].id)
    }

    @Test
    fun `deleteRecebimentosByLocacao - nao afeta outras locacoes`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123"),
            Recebimento(id = "rec-2", locacaoId = "loc-456"),
            Recebimento(id = "rec-3", locacaoId = "loc-789")
        )
        repository.setRecebimentos(recebimentos)

        repository.deleteRecebimentosByLocacao("loc-123")

        val resultado = repository.getRecebimentos().first()
        assertEquals(2, resultado.size)
        assertFalse(resultado.any { it.locacaoId == "loc-123" })
    }

    @Test
    fun `deleteRecebimentosByLocacao - nao falha para locacao inexistente`() = runTest {
        val recebimentos = listOf(
            Recebimento(id = "rec-1", locacaoId = "loc-123")
        )
        repository.setRecebimentos(recebimentos)

        repository.deleteRecebimentosByLocacao("loc-inexistente")

        val resultado = repository.getRecebimentos().first()
        assertEquals(1, resultado.size)
    }

    // ==================== TESTES DE CENARIOS DE NEGOCIO ====================

    @Test
    fun `Cenario - criar locacao e adicionar recebimento`() = runTest {
        val recebimento = Recebimento(
            locacaoId = "loc-nova",
            clienteId = "cli-123",
            equipamentoId = "eq-456",
            valor = 1500.0,
            dataVencimento = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L),
            status = StatusPagamento.PENDENTE,
            numeroRenovacao = 0
        )

        val id = repository.addRecebimento(recebimento)

        val pendentes = repository.getRecebimentosPendentes().first()
        assertEquals(1, pendentes.size)
        assertEquals(1500.0, pendentes[0].valor)
        assertEquals(id, pendentes[0].id)
    }

    @Test
    fun `Cenario - renovar locacao e adicionar novo recebimento`() = runTest {
        // Recebimento original
        repository.addRecebimento(
            Recebimento(
                locacaoId = "loc-123",
                valor = 1500.0,
                status = StatusPagamento.PAGO,
                numeroRenovacao = 0
            )
        )

        // Recebimento da renovação
        repository.addRecebimento(
            Recebimento(
                locacaoId = "loc-123",
                valor = 1600.0,
                status = StatusPagamento.PENDENTE,
                numeroRenovacao = 1
            )
        )

        val recebimentosLocacao = repository.getRecebimentosByLocacao("loc-123").first()

        assertEquals(2, recebimentosLocacao.size)
        assertEquals(0, recebimentosLocacao[0].numeroRenovacao)
        assertEquals(1, recebimentosLocacao[1].numeroRenovacao)
    }

    @Test
    fun `Cenario - marcar pagamento pendente como pago`() = runTest {
        repository.addRecebimento(
            Recebimento(
                locacaoId = "loc-123",
                valor = 1500.0,
                status = StatusPagamento.PENDENTE
            )
        )

        var pendentes = repository.getRecebimentosPendentes().first()
        assertEquals(1, pendentes.size)

        val recebimentoId = pendentes[0].id
        repository.marcarPago(recebimentoId)

        pendentes = repository.getRecebimentosPendentes().first()
        val pagos = repository.getRecebimentosPagos().first()

        assertTrue(pendentes.isEmpty())
        assertEquals(1, pagos.size)
        assertNotNull(pagos[0].dataPagamento)
    }

    @Test
    fun `Cenario - cancelar locacao e remover recebimentos`() = runTest {
        // Adiciona recebimentos para uma locação
        repository.addRecebimento(Recebimento(locacaoId = "loc-cancelar", numeroRenovacao = 0))
        repository.addRecebimento(Recebimento(locacaoId = "loc-cancelar", numeroRenovacao = 1))
        repository.addRecebimento(Recebimento(locacaoId = "loc-manter", numeroRenovacao = 0))

        var todos = repository.getRecebimentos().first()
        assertEquals(3, todos.size)

        // Cancela locação
        repository.deleteRecebimentosByLocacao("loc-cancelar")

        todos = repository.getRecebimentos().first()
        assertEquals(1, todos.size)
        assertEquals("loc-manter", todos[0].locacaoId)
    }
}
