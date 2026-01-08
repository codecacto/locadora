package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.core.pdf.ReceiptPdfGenerator
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepository
import br.com.codecacto.locadora.data.repository.DadosEmpresaRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

private val iosDataModule = module {
    single<DadosEmpresaRepository> { DadosEmpresaRepositoryImpl() }
    single { ReceiptPdfGenerator() }
}

actual fun platformModules(): List<Module> = listOf(iosDataModule)
