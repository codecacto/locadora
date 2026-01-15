package br.com.codecacto.locadora.data.purchase

import br.com.codecacto.locadora.domain.model.PremiumPlan
import br.com.codecacto.locadora.domain.model.PurchaseErrorCode
import br.com.codecacto.locadora.domain.model.PurchaseProduct
import br.com.codecacto.locadora.domain.model.PurchaseResult
import br.com.codecacto.locadora.domain.model.RestoreResult
import br.com.codecacto.locadora.domain.model.SubscriptionInfo
import br.com.codecacto.locadora.domain.repository.PurchaseRepository
import com.revenuecat.purchases.kmp.CacheFetchPolicy
import com.revenuecat.purchases.kmp.CustomerInfo
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesError
import com.revenuecat.purchases.kmp.PurchasesErrorCode
import com.revenuecat.purchases.kmp.awaitCustomerInfo
import com.revenuecat.purchases.kmp.awaitOfferings
import com.revenuecat.purchases.kmp.awaitPurchase
import com.revenuecat.purchases.kmp.awaitRestore
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Instant

class RevenueCatPurchaseRepository : PurchaseRepository {

    private val _subscriptionState = MutableStateFlow(SubscriptionInfo(isActive = false))
    override val subscriptionState: Flow<SubscriptionInfo> = _subscriptionState.asStateFlow()

    private var cachedProducts: Map<String, StoreProduct> = emptyMap()

    override suspend fun isPremium(): Boolean {
        return try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo(CacheFetchPolicy.CACHED_OR_FETCHED)
            customerInfo.entitlements[RevenueCatConfig.ENTITLEMENT_PREMIUM]?.isActive == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getProducts(): Result<List<PurchaseProduct>> {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val currentOffering = offerings.current

            if (currentOffering == null) {
                return Result.success(emptyList())
            }

            val products = currentOffering.availablePackages.mapNotNull { pkg ->
                val product = pkg.storeProduct
                cachedProducts = cachedProducts + (product.id to product)

                val subscriptionOptions = product.subscriptionOptions
                val hasFreeTrial = subscriptionOptions?.freeTrial != null
                val freeTrialPeriod = subscriptionOptions?.freeTrial?.billingPeriod?.let { period ->
                    "${period.value} ${period.unit.name.lowercase()}"
                }

                PurchaseProduct(
                    id = product.id,
                    title = product.title,
                    description = product.description,
                    price = product.price.formatted,
                    priceAmountMicros = (product.price.amountMicros),
                    currencyCode = product.price.currencyCode,
                    hasFreeTrial = hasFreeTrial,
                    freeTrialPeriod = freeTrialPeriod
                )
            }

            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun purchase(plan: PremiumPlan): PurchaseResult {
        return try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val currentOffering = offerings.current
                ?: return PurchaseResult.Error("Offering não encontrado", PurchaseErrorCode.PRODUCT_NOT_FOUND)

            val packageToPurchase = currentOffering.availablePackages.find { pkg ->
                pkg.storeProduct.id == plan.id
            } ?: return PurchaseResult.Error("Produto não encontrado", PurchaseErrorCode.PRODUCT_NOT_FOUND)

            val purchaseResult = Purchases.sharedInstance.awaitPurchase(packageToPurchase)
            val subscriptionInfo = mapCustomerInfoToSubscriptionInfo(purchaseResult.customerInfo)
            _subscriptionState.value = subscriptionInfo

            PurchaseResult.Success(subscriptionInfo)
        } catch (e: PurchasesError) {
            when (e.code) {
                PurchasesErrorCode.PurchaseCancelledError -> PurchaseResult.Cancelled
                PurchasesErrorCode.NetworkError -> PurchaseResult.Error(
                    e.message ?: "Erro de rede",
                    PurchaseErrorCode.NETWORK_ERROR
                )
                PurchasesErrorCode.StoreProblemError -> PurchaseResult.Error(
                    e.message ?: "Erro na loja",
                    PurchaseErrorCode.STORE_ERROR
                )
                PurchasesErrorCode.ProductAlreadyPurchasedError -> PurchaseResult.Error(
                    "Você já possui esta assinatura",
                    PurchaseErrorCode.ALREADY_OWNED
                )
                PurchasesErrorCode.PaymentPendingError -> PurchaseResult.Error(
                    "Pagamento pendente",
                    PurchaseErrorCode.PAYMENT_PENDING
                )
                else -> PurchaseResult.Error(
                    e.message ?: "Erro desconhecido",
                    PurchaseErrorCode.UNKNOWN
                )
            }
        } catch (e: Exception) {
            PurchaseResult.Error(e.message ?: "Erro desconhecido", PurchaseErrorCode.UNKNOWN)
        }
    }

    override suspend fun restorePurchases(): RestoreResult {
        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            val subscriptionInfo = mapCustomerInfoToSubscriptionInfo(customerInfo)
            _subscriptionState.value = subscriptionInfo

            if (subscriptionInfo.isActive) {
                RestoreResult.Success(subscriptionInfo)
            } else {
                RestoreResult.NoPurchasesToRestore
            }
        } catch (e: Exception) {
            RestoreResult.Error(e.message ?: "Erro ao restaurar compras")
        }
    }

    override suspend fun getSubscriptionInfo(): SubscriptionInfo {
        return try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo(CacheFetchPolicy.CACHED_OR_FETCHED)
            mapCustomerInfoToSubscriptionInfo(customerInfo)
        } catch (e: Exception) {
            SubscriptionInfo(isActive = false)
        }
    }

    override suspend fun syncSubscriptionState() {
        try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo(CacheFetchPolicy.FETCH_CURRENT)
            _subscriptionState.value = mapCustomerInfoToSubscriptionInfo(customerInfo)
        } catch (e: Exception) {
            // Silently fail sync
        }
    }

    private fun mapCustomerInfoToSubscriptionInfo(customerInfo: CustomerInfo): SubscriptionInfo {
        val premiumEntitlement = customerInfo.entitlements[RevenueCatConfig.ENTITLEMENT_PREMIUM]
        val isActive = premiumEntitlement?.isActive == true

        return SubscriptionInfo(
            isActive = isActive,
            productId = premiumEntitlement?.productIdentifier,
            expirationDate = premiumEntitlement?.expirationDate?.let {
                Instant.fromEpochMilliseconds(it.toEpochMilliseconds())
            },
            willRenew = premiumEntitlement?.willRenew == true,
            isInTrial = premiumEntitlement?.periodType?.name?.contains("TRIAL", ignoreCase = true) == true,
            trialEndDate = null // RevenueCat não expõe diretamente, mas poderia ser calculado
        )
    }
}
