package br.com.codecacto.locadora.features.subscription.presentation

import androidx.lifecycle.viewModelScope
import br.com.codecacto.locadora.core.base.BaseViewModel
import br.com.codecacto.locadora.core.error.ErrorHandler
import br.com.codecacto.locadora.domain.model.PremiumPlan
import br.com.codecacto.locadora.domain.model.PurchaseResult
import br.com.codecacto.locadora.domain.model.RestoreResult
import br.com.codecacto.locadora.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val purchaseRepository: PurchaseRepository,
    errorHandler: ErrorHandler
) : BaseViewModel<SubscriptionContract.State, SubscriptionContract.Effect, SubscriptionContract.Action>(errorHandler) {

    private val _state = MutableStateFlow(SubscriptionContract.State())
    override val state: StateFlow<SubscriptionContract.State> = _state.asStateFlow()

    companion object {
        private const val TERMS_URL = "https://codecacto.com.br/locadora/termos"
        private const val PRIVACY_URL = "https://codecacto.com.br/locadora/privacidade"
    }

    init {
        loadProducts()
        observeSubscriptionState()
    }

    override fun onAction(action: SubscriptionContract.Action) {
        when (action) {
            is SubscriptionContract.Action.SelectPlan -> {
                _state.value = _state.value.copy(selectedPlan = action.plan)
            }
            is SubscriptionContract.Action.Purchase -> purchase()
            is SubscriptionContract.Action.RestorePurchases -> restorePurchases()
            is SubscriptionContract.Action.LoadProducts -> loadProducts()
            is SubscriptionContract.Action.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
            is SubscriptionContract.Action.OpenTermsOfUse -> {
                emitEffect(SubscriptionContract.Effect.OpenUrl(TERMS_URL))
            }
            is SubscriptionContract.Action.OpenPrivacyPolicy -> {
                emitEffect(SubscriptionContract.Effect.OpenUrl(PRIVACY_URL))
            }
        }
    }

    private fun observeSubscriptionState() {
        viewModelScope.launch {
            purchaseRepository.subscriptionState.collect { info ->
                _state.value = _state.value.copy(
                    subscriptionInfo = info,
                    isPremium = info.isActive
                )
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProductsLoading = true)

            // Sincroniza estado da subscription
            purchaseRepository.syncSubscriptionState()

            // Carrega produtos disponíveis
            purchaseRepository.getProducts()
                .onSuccess { products ->
                    _state.value = _state.value.copy(
                        isProductsLoading = false,
                        productsReady = true,
                        products = products
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isProductsLoading = false,
                        productsReady = false,
                        error = "Erro ao carregar produtos: ${error.message}"
                    )
                }
        }
    }

    private fun purchase() {
        val selectedPlan = _state.value.selectedPlan

        viewModelScope.launch {
            _state.value = _state.value.copy(isPurchasing = true, error = null)

            when (val result = purchaseRepository.purchase(selectedPlan)) {
                is PurchaseResult.Success -> {
                    _state.value = _state.value.copy(
                        isPurchasing = false,
                        isPremium = true,
                        subscriptionInfo = result.subscriptionInfo
                    )
                    emitEffect(SubscriptionContract.Effect.PurchaseSuccess)
                }
                is PurchaseResult.Error -> {
                    _state.value = _state.value.copy(
                        isPurchasing = false,
                        error = result.message
                    )
                    emitEffect(SubscriptionContract.Effect.PurchaseError(mapErrorMessage(result.message)))
                }
                is PurchaseResult.Cancelled -> {
                    _state.value = _state.value.copy(isPurchasing = false)
                }
            }
        }
    }

    private fun restorePurchases() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRestoring = true, error = null)

            when (val result = purchaseRepository.restorePurchases()) {
                is RestoreResult.Success -> {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        isPremium = true,
                        subscriptionInfo = result.subscriptionInfo
                    )
                    emitEffect(SubscriptionContract.Effect.RestoreSuccess)
                }
                is RestoreResult.Error -> {
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        error = result.message
                    )
                    emitEffect(SubscriptionContract.Effect.RestoreError(result.message))
                }
                is RestoreResult.NoPurchasesToRestore -> {
                    _state.value = _state.value.copy(isRestoring = false)
                    emitEffect(SubscriptionContract.Effect.NoPurchasesToRestore)
                }
            }
        }
    }

    private fun mapErrorMessage(message: String): String {
        return when {
            message.contains("network", ignoreCase = true) -> "Erro de conexão. Verifique sua internet."
            message.contains("cancelled", ignoreCase = true) -> "Compra cancelada."
            message.contains("pending", ignoreCase = true) -> "Pagamento pendente de confirmação."
            message.contains("already", ignoreCase = true) -> "Você já possui esta assinatura ativa."
            else -> message
        }
    }
}
