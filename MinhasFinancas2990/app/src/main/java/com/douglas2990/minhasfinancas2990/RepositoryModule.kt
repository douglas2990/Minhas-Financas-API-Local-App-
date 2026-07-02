package com.douglas2990.minhasfinancas2990

import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import com.douglas2990.minhasfinancas2990.data.repository.TransacaoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransacaoRepository(
        transacaoRepositoryImpl: TransacaoRepositoryImpl
    ): TransacaoRepository
}