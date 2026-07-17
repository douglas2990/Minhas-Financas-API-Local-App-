package com.douglas2990.minhasfinancas2990.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import androidx.hilt.work.HiltWorkerFactory
import javax.inject.Singleton

// Este módulo não precisa de código adicional —
// o HiltWorkerFactory é gerado automaticamente pelo hilt-work.
// O importante é que o Application implemente Configuration.Provider
// (veja MinhasFinancasApplication.kt)
@Module
@InstallIn(SingletonComponent::class)
object WorkerModule