package br.com.codecacto.locadora.core.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Ajusta o timestamp retornado pelo Material 3 DatePicker.
 *
 * O DatePicker retorna meia-noite UTC do dia selecionado.
 * Quando convertido para o timezone local (ex: America/Sao_Paulo = UTC-3),
 * a data "volta" para o dia anterior (21:00 do dia anterior).
 *
 * Esta função corrige isso extraindo a data no timezone UTC e
 * recriando o timestamp para meia-noite no timezone local.
 *
 * Exemplo:
 * - Usuário seleciona: 15/01/2025
 * - DatePicker retorna: 15/01/2025 00:00:00 UTC (timestamp)
 * - Sem correção (São Paulo): 14/01/2025 21:00:00 (dia anterior!)
 * - Com correção: 15/01/2025 00:00:00 (timezone local)
 */
fun adjustDatePickerTimestamp(utcMillis: Long): Long {
    val instant = Instant.fromEpochMilliseconds(utcMillis)
    val localDate = instant.toLocalDateTime(TimeZone.UTC).date
    return localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

/**
 * Converte um timestamp local para o formato esperado pelo DatePicker (meia-noite UTC).
 * Usado para definir o initialSelectedDateMillis do DatePicker.
 */
fun toDatePickerMillis(localMillis: Long): Long {
    val instant = Instant.fromEpochMilliseconds(localMillis)
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/**
 * Calcula o número de dias entre duas datas, considerando opcionalmente sábados e domingos.
 *
 * @param dataInicioMillis Timestamp de início (em milissegundos)
 * @param dataFimMillis Timestamp de fim (em milissegundos)
 * @param incluiSabado Se true, conta sábados. Se false, ignora sábados.
 * @param incluiDomingo Se true, conta domingos. Se false, ignora domingos.
 * @return Número de dias no período, excluindo sábados/domingos conforme configuração.
 */
fun calcularDiasLocacao(
    dataInicioMillis: Long,
    dataFimMillis: Long,
    incluiSabado: Boolean = true,
    incluiDomingo: Boolean = true
): Int {
    val tz = TimeZone.currentSystemDefault()
    val dataInicio = Instant.fromEpochMilliseconds(dataInicioMillis).toLocalDateTime(tz).date
    val dataFim = Instant.fromEpochMilliseconds(dataFimMillis).toLocalDateTime(tz).date

    var dias = 0
    var currentDate = dataInicio

    while (currentDate < dataFim) {
        val dayOfWeek = currentDate.dayOfWeek
        val contaDia = when (dayOfWeek) {
            DayOfWeek.SATURDAY -> incluiSabado
            DayOfWeek.SUNDAY -> incluiDomingo
            else -> true
        }
        if (contaDia) {
            dias++
        }
        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
    }

    return dias
}
