package br.com.codecacto.locadora.core.pdf

import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.DadosEmpresa
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.Recebimento

data class ReceiptData(
    val locacao: Locacao,
    val cliente: Cliente,
    val equipamentos: List<Equipamento>,
    val dadosEmpresa: DadosEmpresa
)

data class RecebimentoReceiptData(
    val recebimento: Recebimento,
    val cliente: Cliente,
    val equipamentos: List<Equipamento>,
    val dadosEmpresa: DadosEmpresa,
    val locacao: Locacao? = null
)
