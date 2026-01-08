package br.com.codecacto.locadora.di

import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    coreModule,
    dataModule,
    authModule,
    locacoesModule,
    entregasModule,
    recebimentosModule,
    clientesModule,
    equipamentosModule,
    settingsModule
)
