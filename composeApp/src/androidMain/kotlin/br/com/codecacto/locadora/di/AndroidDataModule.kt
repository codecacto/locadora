package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.core.pdf.ReceiptPdfGenerator
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepository
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDataModule = module {
    single<DadosEmpresaRepository> { DadosEmpresaRepositoryImpl(androidContext()) }
    single { ReceiptPdfGenerator() }
}
