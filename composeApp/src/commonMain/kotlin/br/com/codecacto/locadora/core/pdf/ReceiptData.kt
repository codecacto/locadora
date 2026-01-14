package br.com.codecacto.locadora.core.pdf

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.DadosEmpresa
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao

data class ReceiptData(
    val locacao: Locacao,
    val cliente: Cliente,
    val equipamentos: List<Equipamento>,
    val dadosEmpresa: DadosEmpresa
)
