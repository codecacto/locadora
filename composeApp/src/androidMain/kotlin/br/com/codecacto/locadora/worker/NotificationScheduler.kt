package br.com.codecacto.locadora.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleDaily(context: Context, hora: Int, minuto: Int) {
        val workManager = WorkManager.getInstance(context)

        // Cancela trabalho anterior
        workManager.cancelUniqueWork(RecebimentosVencidosWorker.WORK_NAME)

        // Calcular delay inicial até o horário desejado
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Se já passou do horário hoje, agendar para amanhã
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<RecebimentosVencidosWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            RecebimentosVencidosWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(RecebimentosVencidosWorker.WORK_NAME)
    }

    fun parseHorario(horario: String): Pair<Int, Int> {
        val parts = horario.split(":")
        return if (parts.size == 2) {
            Pair(parts[0].toIntOrNull() ?: 8, parts[1].toIntOrNull() ?: 0)
        } else {
            Pair(8, 0)
        }
    }
}
