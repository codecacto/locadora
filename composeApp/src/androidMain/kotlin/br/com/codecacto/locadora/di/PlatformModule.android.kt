package br.com.codecacto.locadora.di

import org.koin.core.module.Module

actual fun platformModules(): List<Module> = listOf(androidDataModule)
