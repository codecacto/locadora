package br.com.codecacto.locadora.data.purchase

import br.com.codecacto.locadora.BuildInfo
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases

actual object RevenueCatInitializer {
    actual fun initialize(userId: String?) {
        // Configura nível de log baseado no ambiente
        Purchases.logLevel = if (BuildInfo.isDebug) LogLevel.DEBUG else LogLevel.WARN

        // Seleciona a chave de API apropriada
        val apiKey = if (BuildInfo.isDebug) {
            RevenueCatConfig.DEBUG_API_KEY
        } else {
            RevenueCatConfig.IOS_API_KEY
        }

        // Configura o RevenueCat
        Purchases.configure(apiKey = apiKey) {
            appUserId = userId
        }
    }
}
