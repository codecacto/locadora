package br.com.codecacto.locadora.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.codecacto.locadora.MainActivity
import br.com.codecacto.locadora.R
import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.core.model.NotificacaoTipo
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import br.com.codecacto.locadora.data.repository.NotificacaoRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecebimentosVencidosWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val locacaoRepository: LocacaoRepository by inject()
    private val notificacaoRepository: NotificacaoRepository by inject()

    companion object {
        const val WORK_NAME = "recebimentos_vencidos_check"
        private const val CHANNEL_ID = "recebimentos_vencidos"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()

            val locacoesAtivas = locacaoRepository.getLocacoesAtivas().first()
            val hoje = System.currentTimeMillis()
            val umDiaEmMillis = 24 * 60 * 60 * 1000L

            var recebimentosVencidos = 0
            var recebimentosProximosVencimento = 0

            for (locacao in locacoesAtivas) {
                if (locacao.statusPagamento == StatusPagamento.PENDENTE) {
                    val dataVencimento = locacao.dataFimPrevista

                    when {
                        dataVencimento < hoje -> {
                            recebimentosVencidos++
                        }
                        dataVencimento - hoje <= umDiaEmMillis -> {
                            recebimentosProximosVencimento++
                        }
                    }
                }
            }

            // Criar notificacoes no app
            if (recebimentosVencidos > 0) {
                val titulo = if (recebimentosVencidos == 1)
                    "1 recebimento vencido"
                else
                    "$recebimentosVencidos recebimentos vencidos"

                val mensagem = "Existem pagamentos pendentes que precisam de atenção."

                criarNotificacaoNoApp(titulo, mensagem, NotificacaoTipo.VENCIMENTO)
                enviarPushNotification(titulo, mensagem)
            }

            if (recebimentosProximosVencimento > 0) {
                val titulo = if (recebimentosProximosVencimento == 1)
                    "1 recebimento vence hoje"
                else
                    "$recebimentosProximosVencimento recebimentos vencem hoje"

                val mensagem = "Verifique os pagamentos que vencem em breve."

                criarNotificacaoNoApp(titulo, mensagem, NotificacaoTipo.PAGAMENTO)
                enviarPushNotification(titulo, mensagem)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun criarNotificacaoNoApp(titulo: String, mensagem: String, tipo: NotificacaoTipo) {
        val notificacao = Notificacao(
            titulo = titulo,
            mensagem = mensagem,
            tipo = tipo.valor,
            lida = false
        )
        notificacaoRepository.criarNotificacao(notificacao)
    }

    private fun enviarPushNotification(titulo: String, mensagem: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            notify(NOTIFICATION_ID + System.currentTimeMillis().toInt() % 1000, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recebimentos Vencidos"
            val descriptionText = "Notificações sobre recebimentos vencidos ou próximos do vencimento"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
