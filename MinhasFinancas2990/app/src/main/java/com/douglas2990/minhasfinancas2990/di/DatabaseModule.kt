package com.douglas2990.minhasfinancas2990.di

import android.content.Context
import androidx.room.Room
import com.douglas2990.minhasfinancas2990.data.local.AppDatabase
import com.douglas2990.minhasfinancas2990.data.local.dao.TransacaoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, // Use a classe AppDatabase aqui
            "minhas_financas_db"
        ).build()
    }

    @Provides
    fun provideTransacaoDao(database: AppDatabase): TransacaoDao {
        return database.transacaoDao()
    }
}