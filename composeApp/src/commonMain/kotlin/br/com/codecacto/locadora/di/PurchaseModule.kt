package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.data.purchase.RevenueCatPurchaseRepository
import br.com.codecacto.locadora.domain.repository.PurchaseRepository
import br.com.codecacto.locadora.features.subscription.presentation.SubscriptionViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val purchaseModule = module {
    singleOf(::RevenueCatPurchaseRepository) bind PurchaseRepository::class
    viewModelOf(::SubscriptionViewModel)
}
