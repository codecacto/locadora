package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DadosEmpresa(
    val nomeEmpresa: String = "",
    val telefone: String = "",
    val email: String = "",
    val endereco: String = "",
    val cnpj: String = ""
)
