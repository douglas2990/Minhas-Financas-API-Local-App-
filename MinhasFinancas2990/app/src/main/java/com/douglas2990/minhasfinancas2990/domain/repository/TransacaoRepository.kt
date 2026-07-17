package com.douglas2990.minhasfinancas2990.domain.repository

import com.douglas2990.minhasfinancas2990.domain.model.CategoriaDto
import com.douglas2990.minhasfinancas2990.domain.model.MetodoPagamentoDto
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

    suspend fun buscarCategorias(): List<CategoriaDto>
    suspend fun buscarMetodos(): List<MetodoPagamentoDto>

    // Contagem reativa de itens aguardando sync
    fun getPendentesCount(): Flow<Int>

    // Envia UM item específico para a API e marca como sincronizado
    suspend fun sincronizarUm(transacao: Transacao)

    // Remove do Room SEM enviar para API (descarte)
    suspend fun excluirLocal(transacao: Transacao)
}

