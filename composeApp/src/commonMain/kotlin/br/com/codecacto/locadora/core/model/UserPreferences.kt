package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val horarioNotificacao: String = "08:00", // Formato HH:mm
    val momentoPagamentoPadrao: String = "NO_VENCIMENTO" // NO_INICIO ou NO_VENCIMENTO
)
