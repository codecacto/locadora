package br.com.codecacto.locadora.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

// Auth Routes
sealed interface AuthRoute : Route {
    @Serializable
    data object Login : AuthRoute

    @Serializable
    data object Register : AuthRoute

    @Serializable
    data object ForgotPassword : AuthRoute
}

// Main Routes (Bottom Navigation)
sealed interface MainRoute : Route {
    @Serializable
    data object Locacoes : MainRoute

    @Serializable
    data object Entregas : MainRoute

    @Serializable
    data object Recebimentos : MainRoute

    @Serializable
    data object Menu : MainRoute
}

// Locacao Routes
sealed interface LocacaoRoute : Route {
    @Serializable
    data class Detalhes(val locacaoId: String) : LocacaoRoute

    @Serializable
    data object NovaLocacao : LocacaoRoute

    @Serializable
    data class RenovarLocacao(val locacaoId: String) : LocacaoRoute
}

// Cliente Routes
sealed interface ClienteRoute : Route {
    @Serializable
    data object Lista : ClienteRoute

    @Serializable
    data object NovoCliente : ClienteRoute

    @Serializable
    data class EditarCliente(val clienteId: String) : ClienteRoute
}

// Equipamento Routes
sealed interface EquipamentoRoute : Route {
    @Serializable
    data object Lista : EquipamentoRoute

    @Serializable
    data object NovoEquipamento : EquipamentoRoute

    @Serializable
    data class EditarEquipamento(val equipamentoId: String) : EquipamentoRoute
}
