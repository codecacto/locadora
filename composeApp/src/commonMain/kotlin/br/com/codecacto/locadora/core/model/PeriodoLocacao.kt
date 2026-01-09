package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class PeriodoLocacao(val dias: Int, val label: String) {
    DIARIO(1, "Diário"),
    SEMANAL(7, "Semanal"),
    QUINZENAL(15, "Quinzenal"),
    MENSAL(30, "Mensal")
}
