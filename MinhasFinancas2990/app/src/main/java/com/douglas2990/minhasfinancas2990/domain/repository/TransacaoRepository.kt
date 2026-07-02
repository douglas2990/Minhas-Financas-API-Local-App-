package com.douglas2990.minhasfinancas2990.domain.repository

import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import kotlinx.coroutines.flow.Flow

interface TransacaoRepository {
    fun getTransacoes(): Flow<List<Transacao>>

    // Filtra as transações por mês/ano (mes: 1-12)
    fun getTransacoesPorPeriodo(mes: Int, ano: Int): Flow<List<Transacao>>

    // Salva ou adiciona um novo item
    suspend fun salvarTransacao(transacao: Transacao)

    // Remove um item
    suspend fun excluirTransacao(transacao: Transacao)

    suspend fun sincronizar()
}

