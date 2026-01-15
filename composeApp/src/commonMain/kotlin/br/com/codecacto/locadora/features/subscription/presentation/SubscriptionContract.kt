package br.com.codecacto.locadora.features.subscription.presentation

import br.com.codecacto.locadora.core.base.UiAction
import br.com.codecacto.locadora.core.base.UiEffect
import br.com.codecacto.locadora.core.base.UiState
import br.com.codecacto.locadora.domain.model.PremiumPlan
import br.com.codecacto.locadora.domain.model.PurchaseProduct
import br.com.codecacto.locadora.domain.model.SubscriptionInfo

object SubscriptionContract {

    data class State(
        val isLoading: Boolean = false,
        val isProductsLoading: Boolean = true,
        val productsReady: Boolean = false,
        val isPurchasing: Boolean = false,
        val isRestoring: Boolean = false,
        val selectedPlan: PremiumPlan = PremiumPlan.ANUAL,
        val products: List<PurchaseProduct> = emptyList(),
        val subscriptionInfo: SubscriptionInfo? = null,
        val isPremium: Boolean = false,
        val error: String? = null
    ) : UiState

    sealed class Action : UiAction {
        data class SelectPlan(val plan: PremiumPlan) : Action()
        data object Purchase : Action()
        data object RestorePurchases : Action()
        data object LoadProducts : Action()
        data object ClearError : Action()
        data object OpenTermsOfUse : Action()
        data object OpenPrivacyPolicy : Action()
    }

    sealed interface Effect : UiEffect {
        data object PurchaseSuccess : Effect
        data class PurchaseError(val message: String) : Effect
        data object RestoreSuccess : Effect
        data object NoPurchasesToRestore : Effect
        data class RestoreError(val message: String) : Effect
        data class OpenUrl(val url: String) : Effect
    }
}
