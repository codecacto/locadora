package br.com.codecacto.locadora.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Testes unitários para a classe Locacao e seus enums relacionados.
 */
class LocacaoTest {

    // ==================== TESTES DE StatusEntrega ====================

    @Test
    fun `StatusEntrega deve ter 3 valores`() {
        assertEquals(3, StatusEntrega.entries.size)
    }

    @Test
    fun `StatusEntrega valores devem ser corretos`() {
        assertEquals(StatusEntrega.NAO_AGENDADA, StatusEntrega.entries[0])
        assertEquals(StatusEntrega.AGENDADA, StatusEntrega.entries[1])
        assertEquals(StatusEntrega.ENTREGUE, StatusEntrega.entries[2])
    }

    // ==================== TESTES DE StatusPagamento ====================

    @Test
    fun `StatusPagamento deve ter 2 valores`() {
        assertEquals(2, StatusPagamento.entries.size)
    }

    @Test
    fun `StatusPagamento valores devem ser PENDENTE e PAGO`() {
        assertEquals(StatusPagamento.PENDENTE, StatusPagamento.entries[0])
        assertEquals(StatusPagamento.PAGO, StatusPagamento.entries[1])
    }

    // ==================== TESTES DE StatusColeta ====================

    @Test
    fun `StatusColeta deve ter 2 valores`() {
        assertEquals(2, StatusColeta.entries.size)
    }

    @Test
    fun `StatusColeta valores devem ser NAO_COLETADO e COLETADO`() {
        assertEquals(StatusColeta.NAO_COLETADO, StatusColeta.entries[0])
        assertEquals(StatusColeta.COLETADO, StatusColeta.entries[1])
    }

    // ==================== TESTES DE StatusLocacao ====================

    @Test
    fun `StatusLocacao deve ter 2 valores`() {
        assertEquals(2, StatusLocacao.entries.size)
    }

    @Test
    fun `StatusLocacao valores devem ser ATIVA e FINALIZADA`() {
        assertEquals(StatusLocacao.ATIVA, StatusLocacao.entries[0])
        assertEquals(StatusLocacao.FINALIZADA, StatusLocacao.entries[1])
    }

    // ==================== TESTES DE StatusPrazo ====================

    @Test
    fun `StatusPrazo deve ter 3 valores`() {
        assertEquals(3, StatusPrazo.entries.size)
    }

    @Test
    fun `StatusPrazo valores devem ser corretos`() {
        assertEquals(StatusPrazo.NORMAL, StatusPrazo.entries[0])
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, StatusPrazo.entries[1])
        assertEquals(StatusPrazo.VENCIDO, StatusPrazo.entries[2])
    }

    // ==================== TESTES DE Locacao - Valores Padrao ====================

    @Test
    fun `Locacao com valores padrao deve ter campos corretos`() {
        val locacao = Locacao()

        assertEquals("", locacao.id)
        assertEquals("", locacao.clienteId)
        assertEquals("", locacao.equipamentoId)
        assertEquals(0.0, locacao.valorLocacao)
        assertEquals(PeriodoLocacao.DIARIO, locacao.periodo)
        assertEquals(MomentoPagamento.NO_VENCIMENTO, locacao.momentoPagamento)
        assertEquals(StatusEntrega.NAO_AGENDADA, locacao.statusEntrega)
        assertNull(locacao.dataEntregaPrevista)
        assertNull(locacao.dataEntregaReal)
        assertEquals(StatusPagamento.PENDENTE, locacao.statusPagamento)
        assertNull(locacao.dataPagamento)
        assertEquals(StatusColeta.NAO_COLETADO, locacao.statusColeta)
        assertNull(locacao.dataColeta)
        assertEquals(false, locacao.emitirNota)
        assertEquals(false, locacao.notaEmitida)
        assertEquals(StatusLocacao.ATIVA, locacao.statusLocacao)
        assertEquals(0, locacao.qtdRenovacoes)
        assertNull(locacao.ultimaRenovacaoEm)
    }

    @Test
    fun `Locacao COLLECTION_NAME deve ser correto`() {
        assertEquals("locacoes", Locacao.COLLECTION_NAME)
    }

    @Test
    fun `Locacao DIAS_ALERTA_VENCIMENTO deve ser 2`() {
        assertEquals(2, Locacao.DIAS_ALERTA_VENCIMENTO)
    }

    // ==================== TESTES DE Locacao - Criacao ====================

    @Test
    fun `Locacao criada com dados deve manter valores`() {
        val now = System.currentTimeMillis()
        val locacao = Locacao(
            id = "loc-123",
            clienteId = "cli-456",
            equipamentoId = "equip-789",
            valorLocacao = 500.0,
            periodo = PeriodoLocacao.MENSAL,
            momentoPagamento = MomentoPagamento.NO_INICIO,
            dataInicio = now,
            dataFimPrevista = now + (30 * 24 * 60 * 60 * 1000L), // 30 dias
            statusEntrega = StatusEntrega.AGENDADA,
            statusPagamento = StatusPagamento.PAGO,
            statusColeta = StatusColeta.NAO_COLETADO,
            statusLocacao = StatusLocacao.ATIVA,
            qtdRenovacoes = 2,
            emitirNota = true
        )

        assertEquals("loc-123", locacao.id)
        assertEquals("cli-456", locacao.clienteId)
        assertEquals("equip-789", locacao.equipamentoId)
        assertEquals(500.0, locacao.valorLocacao)
        assertEquals(PeriodoLocacao.MENSAL, locacao.periodo)
        assertEquals(MomentoPagamento.NO_INICIO, locacao.momentoPagamento)
        assertEquals(StatusEntrega.AGENDADA, locacao.statusEntrega)
        assertEquals(StatusPagamento.PAGO, locacao.statusPagamento)
        assertEquals(StatusLocacao.ATIVA, locacao.statusLocacao)
        assertEquals(2, locacao.qtdRenovacoes)
        assertEquals(true, locacao.emitirNota)
    }

    @Test
    fun `Locacao copy deve criar nova instancia com valores alterados`() {
        val original = Locacao(
            id = "loc-123",
            clienteId = "cli-456",
            statusPagamento = StatusPagamento.PENDENTE
        )

        val atualizada = original.copy(
            statusPagamento = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )

        // Original deve permanecer inalterada
        assertEquals(StatusPagamento.PENDENTE, original.statusPagamento)
        assertNull(original.dataPagamento)

        // Cópia deve ter valores atualizados
        assertEquals(StatusPagamento.PAGO, atualizada.statusPagamento)
        assertNotNull(atualizada.dataPagamento)

        // Valores não alterados devem ser mantidos
        assertEquals(original.id, atualizada.id)
        assertEquals(original.clienteId, atualizada.clienteId)
    }

    // ==================== TESTES DE Locacao - Transicoes de Status ====================

    @Test
    fun `Locacao transicao de entrega - NAO_AGENDADA para AGENDADA`() {
        val locacao = Locacao(statusEntrega = StatusEntrega.NAO_AGENDADA)
        val agendada = locacao.copy(
            statusEntrega = StatusEntrega.AGENDADA,
            dataEntregaPrevista = System.currentTimeMillis()
        )

        assertEquals(StatusEntrega.AGENDADA, agendada.statusEntrega)
        assertNotNull(agendada.dataEntregaPrevista)
    }

    @Test
    fun `Locacao transicao de entrega - AGENDADA para ENTREGUE`() {
        val locacao = Locacao(
            statusEntrega = StatusEntrega.AGENDADA,
            dataEntregaPrevista = System.currentTimeMillis()
        )
        val entregue = locacao.copy(
            statusEntrega = StatusEntrega.ENTREGUE,
            dataEntregaReal = System.currentTimeMillis()
        )

        assertEquals(StatusEntrega.ENTREGUE, entregue.statusEntrega)
        assertNotNull(entregue.dataEntregaReal)
    }

    @Test
    fun `Locacao transicao de pagamento - PENDENTE para PAGO`() {
        val locacao = Locacao(statusPagamento = StatusPagamento.PENDENTE)
        val paga = locacao.copy(
            statusPagamento = StatusPagamento.PAGO,
            dataPagamento = System.currentTimeMillis()
        )

        assertEquals(StatusPagamento.PAGO, paga.statusPagamento)
        assertNotNull(paga.dataPagamento)
    }

    @Test
    fun `Locacao transicao de coleta - NAO_COLETADO para COLETADO`() {
        val locacao = Locacao(statusColeta = StatusColeta.NAO_COLETADO)
        val coletada = locacao.copy(
            statusColeta = StatusColeta.COLETADO,
            dataColeta = System.currentTimeMillis()
        )

        assertEquals(StatusColeta.COLETADO, coletada.statusColeta)
        assertNotNull(coletada.dataColeta)
    }

    @Test
    fun `Locacao finalizacao - ATIVA para FINALIZADA`() {
        val locacao = Locacao(
            statusLocacao = StatusLocacao.ATIVA,
            statusPagamento = StatusPagamento.PAGO,
            statusColeta = StatusColeta.COLETADO
        )
        val finalizada = locacao.copy(statusLocacao = StatusLocacao.FINALIZADA)

        assertEquals(StatusLocacao.FINALIZADA, finalizada.statusLocacao)
    }

    // ==================== TESTES DE Locacao - Renovacao ====================

    @Test
    fun `Locacao renovacao deve incrementar contador`() {
        val locacao = Locacao(qtdRenovacoes = 0)
        val renovada = locacao.copy(
            qtdRenovacoes = locacao.qtdRenovacoes + 1,
            ultimaRenovacaoEm = System.currentTimeMillis()
        )

        assertEquals(1, renovada.qtdRenovacoes)
        assertNotNull(renovada.ultimaRenovacaoEm)
    }

    @Test
    fun `Locacao multiplas renovacoes deve manter historico`() {
        var locacao = Locacao(qtdRenovacoes = 0)

        for (i in 1..5) {
            locacao = locacao.copy(
                qtdRenovacoes = locacao.qtdRenovacoes + 1,
                ultimaRenovacaoEm = System.currentTimeMillis()
            )
        }

        assertEquals(5, locacao.qtdRenovacoes)
    }

    // ==================== TESTES DE Locacao - Periodos ====================

    @Test
    fun `Locacao com periodo DIARIO deve funcionar corretamente`() {
        val locacao = Locacao(periodo = PeriodoLocacao.DIARIO)
        assertEquals(PeriodoLocacao.DIARIO, locacao.periodo)
        assertEquals(1, locacao.periodo.dias)
    }

    @Test
    fun `Locacao com periodo SEMANAL deve funcionar corretamente`() {
        val locacao = Locacao(periodo = PeriodoLocacao.SEMANAL)
        assertEquals(PeriodoLocacao.SEMANAL, locacao.periodo)
        assertEquals(7, locacao.periodo.dias)
    }

    @Test
    fun `Locacao com periodo QUINZENAL deve funcionar corretamente`() {
        val locacao = Locacao(periodo = PeriodoLocacao.QUINZENAL)
        assertEquals(PeriodoLocacao.QUINZENAL, locacao.periodo)
        assertEquals(15, locacao.periodo.dias)
    }

    @Test
    fun `Locacao com periodo MENSAL deve funcionar corretamente`() {
        val locacao = Locacao(periodo = PeriodoLocacao.MENSAL)
        assertEquals(PeriodoLocacao.MENSAL, locacao.periodo)
        assertEquals(30, locacao.periodo.dias)
    }
}
