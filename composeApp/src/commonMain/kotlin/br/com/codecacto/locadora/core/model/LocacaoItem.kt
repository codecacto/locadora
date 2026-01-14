package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

/**
 * Representa um item de equipamento dentro de uma locação.
 * Permite especificar quantidade e patrimônios específicos.
 */
@Serializable
data class LocacaoItem(
    val equipamentoId: String = "",
    val quantidade: Int = 1,  // Quantidade locada deste equipamento
    val patrimonioIds: List<String> = emptyList()  // IDs dos patrimônios específicos (se aplicável)
)
