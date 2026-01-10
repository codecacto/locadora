package br.com.codecacto.locadora

import android.app.Application
import br.com.codecacto.locadora.data.repository.UserPreferencesRepository
import br.com.codecacto.locadora.di.appModules
import br.com.codecacto.locadora.worker.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LocadoraApplication : Application() {

    companion object {
        lateinit var instance: LocadoraApplication
            private set
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@LocadoraApplication)
            modules(appModules())
        }

        // Schedule daily notification check
        scheduleNotificationWorker()
    }

    private fun scheduleNotificationWorker() {
        applicationScope.launch {
            try {
                val userPreferencesRepository: UserPreferencesRepository by inject()
                val horario = userPreferencesRepository.getHorarioNotificacao().first()
                val (hora, minuto) = NotificationScheduler.parseHorario(horario)
                NotificationScheduler.scheduleDaily(this@LocadoraApplication, hora, minuto)
            } catch (e: Exception) {
                // Se falhar, usa horário padrão
                NotificationScheduler.scheduleDaily(this@LocadoraApplication, 8, 0)
            }
        }
    }
}
