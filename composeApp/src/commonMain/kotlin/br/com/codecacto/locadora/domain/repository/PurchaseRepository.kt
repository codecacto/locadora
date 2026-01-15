package br.com.codecacto.locadora.domain.repository

import br.com.codecacto.locadora.domain.model.PremiumPlan
import br.com.codecacto.locadora.domain.model.PurchaseProduct
import br.com.codecacto.locadora.domain.model.PurchaseResult
import br.com.codecacto.locadora.domain.model.RestoreResult
import br.com.codecacto.locadora.domain.model.SubscriptionInfo
import kotlinx.coroutines.flow.Flow

/**
 * Interface para gerenciamento de compras e assinaturas.
 */
interface PurchaseRepository {
    /**
     * Flow que emite atualizações do estado da assinatura.
     */
    val subscriptionState: Flow<SubscriptionInfo>

    /**
     * Verifica se o usuário tem assinatura premium ativa.
     */
    suspend fun isPremium(): Boolean

    /**
     * Obtém a lista de produtos disponíveis para compra.
     */
    suspend fun getProducts(): Result<List<PurchaseProduct>>

    /**
     * Realiza a compra de um plano premium.
     */
    suspend fun purchase(plan: PremiumPlan): PurchaseResult

    /**
     * Restaura compras anteriores.
     */
    suspend fun restorePurchases(): RestoreResult

    /**
     * Obtém informações detalhadas da assinatura atual.
     */
    suspend fun getSubscriptionInfo(): SubscriptionInfo

    /**
     * Sincroniza o estado da assinatura com o servidor.
     */
    suspend fun syncSubscriptionState()
}
