package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.core.pdf.ReceiptPdfGenerator
import br.com.codecacto.locadora.data.repository.UserPreferencesRepository
import br.com.codecacto.locadora.data.repository.UserPreferencesRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataModule = module {
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }
    single { ReceiptPdfGenerator() }
}
