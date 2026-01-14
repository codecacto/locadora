package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

/**
 * Representa uma unidade específica de um equipamento, identificada por um patrimônio.
 * Usado quando o equipamento precisa de rastreamento individual (ex: betoneiras numeradas).
 */
@Serializable
data class Patrimonio(
    val id: String = "",
    val codigo: String = "",  // Código do patrimônio (ex: "PAT-001", "BET-2024-01")
    val descricao: String? = null  // Descrição opcional (ex: "Betoneira amarela")
)
