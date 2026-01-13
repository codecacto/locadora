package br.com.codecacto.locadora

import androidx.compose.ui.window.ComposeUIViewController
import br.com.codecacto.locadora.di.appModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools

fun initKoin() {
    // Check if Koin is already started to avoid double initialization
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(appModules())
        }
    }
}

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}