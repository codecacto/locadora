package br.com.codecacto.locadora.data.purchase

/**
 * Inicializador do RevenueCat para plataformas específicas.
 * Cada plataforma (Android/iOS) implementa sua própria inicialização.
 */
expect object RevenueCatInitializer {
    /**
     * Inicializa o RevenueCat SDK.
     * @param userId ID opcional do usuário para sincronização de compras
     */
    fun initialize(userId: String? = null)
}
