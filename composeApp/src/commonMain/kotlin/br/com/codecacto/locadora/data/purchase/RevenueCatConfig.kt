package br.com.codecacto.locadora.data.purchase

object RevenueCatConfig {
    // Chaves de API do RevenueCat
    // Debug
    const val DEBUG_API_KEY = "test_YOUR_DEBUG_API_KEY"

    // Release - Android
    const val ANDROID_API_KEY = "goog_YOUR_GOOGLE_API_KEY"

    // Release - iOS
    const val IOS_API_KEY = "appl_YOUR_APPLE_API_KEY"

    // IDs dos produtos configurados no RevenueCat
    object Products {
        const val PREMIUM_MONTHLY = "locadora_premium_mensal"
        const val PREMIUM_ANNUAL = "locadora_premium_anual"
    }

    // Entitlement que representa acesso premium
    const val ENTITLEMENT_PREMIUM = "premium"
}
