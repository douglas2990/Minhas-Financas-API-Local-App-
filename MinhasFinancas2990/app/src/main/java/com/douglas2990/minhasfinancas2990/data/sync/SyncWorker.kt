package com.douglas2990.minhasfinancas2990.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SYNC_WORKER", "WorkManager iniciou a sincronização...")
            syncManager.triggerSync()
            Log.d("SYNC_WORKER", "Sincronização concluída com sucesso.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SYNC_WORKER", "Falha na sincronização: ${e.message}", e)
            // Retry automático pelo WorkManager se falhar
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_transacoes"
    }
}