package com.douglas2990.minhasfinancas2990

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.douglas2990.minhasfinancas2990.data.sync.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MinhasFinancasApplication : Application(), Configuration.Provider {

    // Hilt injeta a factory que permite usar @HiltWorker no SyncWorker
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Agenda sync periódica + imediata ao iniciar o app
        WorkManagerScheduler.agendarSync(this)
    }
}