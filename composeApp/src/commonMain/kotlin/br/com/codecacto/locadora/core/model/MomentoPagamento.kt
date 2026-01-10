package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class MomentoPagamento(val label: String) {
    NO_INICIO("No Início"),
    NO_VENCIMENTO("No Vencimento")
}
