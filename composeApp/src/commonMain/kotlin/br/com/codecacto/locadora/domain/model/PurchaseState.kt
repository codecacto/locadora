package br.com.codecacto.locadora.domain.model

import kotlinx.datetime.Instant

/**
 * Informações sobre a assinatura do usuário.
 */
data class SubscriptionInfo(
    val isActive: Boolean,
    val productId: String? = null,
    val expirationDate: Instant? = null,
    val willRenew: Boolean = false,
    val isInTrial: Boolean = false,
    val trialEndDate: Instant? = null
)

/**
 * Informações de um produto disponível para compra.
 */
data class PurchaseProduct(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long,
    val currencyCode: String,
    val hasFreeTrial: Boolean = false,
    val freeTrialPeriod: String? = null
)

/**
 * Resultado de uma operação de compra.
 */
sealed class PurchaseResult {
    data class Success(val subscriptionInfo: SubscriptionInfo) : PurchaseResult()
    data class Error(val message: String, val code: PurchaseErrorCode) : PurchaseResult()
    data object Cancelled : PurchaseResult()
}

/**
 * Resultado de uma operação de restauração de compras.
 */
sealed class RestoreResult {
    data class Success(val subscriptionInfo: SubscriptionInfo) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
    data object NoPurchasesToRestore : RestoreResult()
}

/**
 * Códigos de erro para operações de compra.
 */
enum class PurchaseErrorCode {
    NETWORK_ERROR,
    STORE_ERROR,
    PRODUCT_NOT_FOUND,
    PAYMENT_PENDING,
    PAYMENT_DECLINED,
    ALREADY_OWNED,
    UNKNOWN
}
