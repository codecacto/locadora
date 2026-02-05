package br.com.codecacto.locadora.core.data

/**
 * Representa um estado brasileiro.
 */
data class BrazilianState(
    val abbreviation: String,
    val name: String
)

/**
 * Lista de estados brasileiros.
 */
object BrazilianStates {

    val all: List<BrazilianState> = listOf(
        BrazilianState("AC", "Acre"),
        BrazilianState("AL", "Alagoas"),
        BrazilianState("AP", "Amapá"),
        BrazilianState("AM", "Amazonas"),
        BrazilianState("BA", "Bahia"),
        BrazilianState("CE", "Ceará"),
        BrazilianState("DF", "Distrito Federal"),
        BrazilianState("ES", "Espírito Santo"),
        BrazilianState("GO", "Goiás"),
        BrazilianState("MA", "Maranhão"),
        BrazilianState("MT", "Mato Grosso"),
        BrazilianState("MS", "Mato Grosso do Sul"),
        BrazilianState("MG", "Minas Gerais"),
        BrazilianState("PA", "Pará"),
        BrazilianState("PB", "Paraíba"),
        BrazilianState("PR", "Paraná"),
        BrazilianState("PE", "Pernambuco"),
        BrazilianState("PI", "Piauí"),
        BrazilianState("RJ", "Rio de Janeiro"),
        BrazilianState("RN", "Rio Grande do Norte"),
        BrazilianState("RS", "Rio Grande do Sul"),
        BrazilianState("RO", "Rondônia"),
        BrazilianState("RR", "Roraima"),
        BrazilianState("SC", "Santa Catarina"),
        BrazilianState("SP", "São Paulo"),
        BrazilianState("SE", "Sergipe"),
        BrazilianState("TO", "Tocantins")
    )

    val abbreviations: List<String> = all.map { it.abbreviation }

    fun findByAbbreviation(abbreviation: String): BrazilianState? =
        all.find { it.abbreviation.equals(abbreviation, ignoreCase = true) }
}
