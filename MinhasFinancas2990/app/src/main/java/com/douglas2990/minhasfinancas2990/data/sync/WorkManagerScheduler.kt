package com.douglas2990.minhasfinancas2990.data.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    /**
     * Agenda sincronização periódica (a cada 15 min) E imediata.
     * Ambas só rodam quando há rede disponível (Constraint de rede).
     * Chamado no Application.onCreate() e sempre que o usuário salvar offline.
     */
    fun agendarSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // só roda com rede
            .build()

        // Sync imediata (OneTimeWork) — roda assim que a rede aparecer
        val syncImediata = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        // Sync periódica (15 min é o mínimo permitido pelo Android)
        val syncPeriodica = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)

        // Enqueue imediata (substitui qualquer pendente anterior)
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME + "_imediata",
            ExistingWorkPolicy.REPLACE,
            syncImediata
        )

        // Enqueue periódica (mantém se já estiver rodando)
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME + "_periodica",
            ExistingPeriodicWorkPolicy.KEEP,
            syncPeriodica
        )
    }
}