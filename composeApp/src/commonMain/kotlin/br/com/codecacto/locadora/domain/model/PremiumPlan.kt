package br.com.codecacto.locadora.domain.model

/**
 * Planos de assinatura disponíveis no aplicativo.
 */
enum class PremiumPlan(
    val id: String,
    val displayName: String,
    val description: String,
    val durationMonths: Int,
    val price: Double,
    val pricePerMonth: Double,
    val savingsPercent: Int,
    val isRecommended: Boolean
) {
    MENSAL(
        id = "locadora_premium_mensal",
        displayName = "Mensal",
        description = "Flexibilidade total",
        durationMonths = 1,
        price = 49.90,
        pricePerMonth = 49.90,
        savingsPercent = 0,
        isRecommended = false
    ),
    ANUAL(
        id = "locadora_premium_anual",
        displayName = "Anual",
        description = "Melhor custo-benefício",
        durationMonths = 12,
        price = 399.90,
        pricePerMonth = 33.33,
        savingsPercent = 33,
        isRecommended = true
    );

    companion object {
        fun fromId(id: String): PremiumPlan? = entries.find { it.id == id }
    }
}
