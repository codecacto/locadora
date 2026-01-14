package br.com.codecacto.locadora.core.model

/**
 * Representa a disponibilidade de um equipamento para locação.
 */
data class DisponibilidadeEquipamento(
    val equipamento: Equipamento,
    val quantidadeTotal: Int,
    val quantidadeAlugada: Int,
    val patrimoniosAlugadosIds: Set<String> = emptySet()  // IDs dos patrimônios já alugados
) {
    val quantidadeDisponivel: Int
        get() = quantidadeTotal - quantidadeAlugada

    val isDisponivel: Boolean
        get() = quantidadeDisponivel > 0

    /**
     * Retorna a lista de patrimônios disponíveis para locação.
     */
    fun getPatrimoniosDisponiveis(): List<Patrimonio> {
        return equipamento.patrimonios.filter { it.id !in patrimoniosAlugadosIds }
    }
}
