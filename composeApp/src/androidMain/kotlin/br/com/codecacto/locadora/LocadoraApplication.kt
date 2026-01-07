package br.com.codecacto.locadora

import android.app.Application
import br.com.codecacto.locadora.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LocadoraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@LocadoraApplication)
            modules(appModules())
        }
    }
}
