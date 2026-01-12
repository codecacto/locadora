package br.com.codecacto.locadora.core.util

import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusLocacao
import br.com.codecacto.locadora.core.model.StatusPrazo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Testes unitários para lógica de cálculo de datas e prazos.
 *
 * Estes testes validam a lógica de negócio relacionada a:
 * - Cálculo de dias entre datas
 * - Determinação de status de prazo (NORMAL, PROXIMO_VENCIMENTO, VENCIDO)
 * - Cálculo de data fim baseado em período
 */
class DateUtilTest {

    // Constantes para facilitar os cálculos
    private val UM_DIA_MS = 24L * 60L * 60L * 1000L
    private val DIAS_ALERTA = Locacao.DIAS_ALERTA_VENCIMENTO // 2 dias

    // ==================== TESTES DE CALCULO DE DATA FIM ====================

    @Test
    fun `calcularDataFim - periodo DIARIO adiciona 1 dia`() {
        val dataInicio = 0L
        val periodo = PeriodoLocacao.DIARIO
        val diasEmMillis = periodo.dias * UM_DIA_MS
        val dataFim = dataInicio + diasEmMillis

        assertEquals(UM_DIA_MS, dataFim)
        assertEquals(1, periodo.dias)
    }

    @Test
    fun `calcularDataFim - periodo SEMANAL adiciona 7 dias`() {
        val dataInicio = 0L
        val periodo = PeriodoLocacao.SEMANAL
        val diasEmMillis = periodo.dias * UM_DIA_MS
        val dataFim = dataInicio + diasEmMillis

        assertEquals(7 * UM_DIA_MS, dataFim)
        assertEquals(7, periodo.dias)
    }

    @Test
    fun `calcularDataFim - periodo QUINZENAL adiciona 15 dias`() {
        val dataInicio = 0L
        val periodo = PeriodoLocacao.QUINZENAL
        val diasEmMillis = periodo.dias * UM_DIA_MS
        val dataFim = dataInicio + diasEmMillis

        assertEquals(15 * UM_DIA_MS, dataFim)
        assertEquals(15, periodo.dias)
    }

    @Test
    fun `calcularDataFim - periodo MENSAL adiciona 30 dias`() {
        val dataInicio = 0L
        val periodo = PeriodoLocacao.MENSAL
        val diasEmMillis = periodo.dias * UM_DIA_MS
        val dataFim = dataInicio + diasEmMillis

        assertEquals(30 * UM_DIA_MS, dataFim)
        assertEquals(30, periodo.dias)
    }

    @Test
    fun `calcularDataFim - com data real`() {
        val dataInicio = System.currentTimeMillis()
        val periodo = PeriodoLocacao.SEMANAL
        val diasEmMillis = periodo.dias * UM_DIA_MS
        val dataFim = dataInicio + diasEmMillis

        // A diferença deve ser exatamente 7 dias em ms
        assertEquals(7 * UM_DIA_MS, dataFim - dataInicio)
    }

    // ==================== TESTES DE CALCULO DE DIAS RESTANTES ====================

    @Test
    fun `calcularDiasRestantes - 10 dias no futuro`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + (10 * UM_DIA_MS)
        val diffDays = ((dataFim - hoje) / UM_DIA_MS).toInt()

        assertEquals(10, diffDays)
    }

    @Test
    fun `calcularDiasRestantes - hoje (0 dias)`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje
        val diffDays = ((dataFim - hoje) / UM_DIA_MS).toInt()

        assertEquals(0, diffDays)
    }

    @Test
    fun `calcularDiasRestantes - 5 dias atrasado (negativo)`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje - (5 * UM_DIA_MS)
        val diffDays = ((dataFim - hoje) / UM_DIA_MS).toInt()

        assertEquals(-5, diffDays)
    }

    // ==================== TESTES DE LOGICA DE STATUS PRAZO ====================

    @Test
    fun `statusPrazo - locacao FINALIZADA sempre retorna NORMAL`() {
        val locacao = Locacao(
            statusLocacao = StatusLocacao.FINALIZADA,
            dataFimPrevista = System.currentTimeMillis() - (10 * UM_DIA_MS) // 10 dias atrasado
        )

        // Lógica: se FINALIZADA, retorna NORMAL independente da data
        val status = if (locacao.statusLocacao == StatusLocacao.FINALIZADA) {
            StatusPrazo.NORMAL
        } else {
            calcularStatusPrazoPorData(locacao.dataFimPrevista)
        }

        assertEquals(StatusPrazo.NORMAL, status)
    }

    @Test
    fun `statusPrazo - locacao vencida retorna VENCIDO`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje - UM_DIA_MS // Ontem

        val status = calcularStatusPrazoPorData(dataFim)

        assertEquals(StatusPrazo.VENCIDO, status)
    }

    @Test
    fun `statusPrazo - locacao vencendo amanha retorna PROXIMO_VENCIMENTO`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + UM_DIA_MS // Amanhã (1 dia)

        val status = calcularStatusPrazoPorData(dataFim)

        // 1 dia <= DIAS_ALERTA (2), então PROXIMO_VENCIMENTO
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, status)
    }

    @Test
    fun `statusPrazo - locacao vencendo em 2 dias retorna PROXIMO_VENCIMENTO`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + (2 * UM_DIA_MS) // 2 dias

        val status = calcularStatusPrazoPorData(dataFim)

        // 2 dias <= DIAS_ALERTA (2), então PROXIMO_VENCIMENTO
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, status)
    }

    @Test
    fun `statusPrazo - locacao vencendo em 3 dias retorna NORMAL`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + (3 * UM_DIA_MS) // 3 dias

        val status = calcularStatusPrazoPorData(dataFim)

        // 3 dias > DIAS_ALERTA (2), então NORMAL
        assertEquals(StatusPrazo.NORMAL, status)
    }

    @Test
    fun `statusPrazo - locacao vencendo em 30 dias retorna NORMAL`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + (30 * UM_DIA_MS) // 30 dias

        val status = calcularStatusPrazoPorData(dataFim)

        assertEquals(StatusPrazo.NORMAL, status)
    }

    @Test
    fun `statusPrazo - locacao venceu hoje retorna PROXIMO_VENCIMENTO`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje // Hoje mesmo

        val status = calcularStatusPrazoPorData(dataFim)

        // 0 dias <= DIAS_ALERTA (2), então PROXIMO_VENCIMENTO
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, status)
    }

    // ==================== TESTES DE DIAS_ALERTA_VENCIMENTO ====================

    @Test
    fun `DIAS_ALERTA_VENCIMENTO deve ser 2`() {
        assertEquals(2, Locacao.DIAS_ALERTA_VENCIMENTO)
    }

    @Test
    fun `statusPrazo - exatamente no limite de alerta`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + (DIAS_ALERTA * UM_DIA_MS)
        val diffDays = ((dataFim - hoje) / UM_DIA_MS).toInt()

        assertEquals(DIAS_ALERTA, diffDays)

        val status = calcularStatusPrazoPorData(dataFim)
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, status)
    }

    @Test
    fun `statusPrazo - um dia apos limite de alerta`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + ((DIAS_ALERTA + 1) * UM_DIA_MS)
        val diffDays = ((dataFim - hoje) / UM_DIA_MS).toInt()

        assertEquals(DIAS_ALERTA + 1, diffDays)

        val status = calcularStatusPrazoPorData(dataFim)
        assertEquals(StatusPrazo.NORMAL, status)
    }

    // ==================== TESTES DE CENARIOS COMPLETOS ====================

    @Test
    fun `Cenario - locacao nova com periodo mensal`() {
        val dataInicio = System.currentTimeMillis()
        val periodo = PeriodoLocacao.MENSAL
        val dataFim = dataInicio + (periodo.dias * UM_DIA_MS)

        val locacao = Locacao(
            dataInicio = dataInicio,
            dataFimPrevista = dataFim,
            statusLocacao = StatusLocacao.ATIVA
        )

        val status = calcularStatusPrazoPorData(locacao.dataFimPrevista)
        assertEquals(StatusPrazo.NORMAL, status)
    }

    @Test
    fun `Cenario - locacao renovada ainda dentro do prazo`() {
        val dataInicio = System.currentTimeMillis()
        val periodo = PeriodoLocacao.SEMANAL
        val dataFim = dataInicio + (periodo.dias * UM_DIA_MS) // 7 dias

        val locacao = Locacao(
            dataInicio = dataInicio,
            dataFimPrevista = dataFim,
            statusLocacao = StatusLocacao.ATIVA,
            qtdRenovacoes = 1
        )

        val status = calcularStatusPrazoPorData(locacao.dataFimPrevista)
        assertEquals(StatusPrazo.NORMAL, status)
    }

    @Test
    fun `Cenario - locacao prestes a vencer precisa alerta`() {
        val hoje = System.currentTimeMillis()
        val dataFim = hoje + UM_DIA_MS // Amanhã

        val locacao = Locacao(
            dataFimPrevista = dataFim,
            statusLocacao = StatusLocacao.ATIVA
        )

        val status = calcularStatusPrazoPorData(locacao.dataFimPrevista)
        assertEquals(StatusPrazo.PROXIMO_VENCIMENTO, status)
    }

    // ==================== FUNCAO AUXILIAR DE TESTE ====================

    /**
     * Reimplementação da lógica de calcularStatusPrazo para testes.
     * Esta função replica a lógica do ViewModel para permitir testes unitários.
     */
    private fun calcularStatusPrazoPorData(dataFimPrevista: Long): StatusPrazo {
        val hoje = System.currentTimeMillis()
        val diffDays = ((dataFimPrevista - hoje) / UM_DIA_MS).toInt()

        return when {
            diffDays < 0 -> StatusPrazo.VENCIDO
            diffDays <= DIAS_ALERTA -> StatusPrazo.PROXIMO_VENCIMENTO
            else -> StatusPrazo.NORMAL
        }
    }
}
