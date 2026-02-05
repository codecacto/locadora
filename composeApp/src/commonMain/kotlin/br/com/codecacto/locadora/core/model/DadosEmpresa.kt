package br.com.codecacto.locadora.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DadosEmpresa(
    val nomeEmpresa: String = "",
    val telefone: String = "",
    val email: String = "",
    val endereco: String = "", // Rua, número e bairro
    val cidade: String = "",
    val estado: String = "", // UF (sigla do estado)
    val documento: String = "", // CPF ou CNPJ
    val tipoPessoa: String = "JURIDICA" // FISICA ou JURIDICA
)
