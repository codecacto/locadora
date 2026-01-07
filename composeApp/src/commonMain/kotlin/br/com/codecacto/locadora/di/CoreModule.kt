package br.com.codecacto.locadora.di

import br.com.codecacto.locadora.core.error.DefaultErrorHandler
import br.com.codecacto.locadora.core.error.ErrorHandler
import org.koin.dsl.module

val coreModule = module {
    single<ErrorHandler> { DefaultErrorHandler() }
}
