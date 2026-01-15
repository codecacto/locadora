package br.com.codecacto.locadora.di

import org.koin.core.module.Module

fun appModules(): List<Module> = listOf(
    coreModule,
    dataModule,
    authModule,
    purchaseModule,
    locacoesModule,
    entregasModule,
    recebimentosModule,
    clientesModule,
    equipamentosModule,
    settingsModule,
    feedbackModule,
    notificationsModule
) + platformModules()
