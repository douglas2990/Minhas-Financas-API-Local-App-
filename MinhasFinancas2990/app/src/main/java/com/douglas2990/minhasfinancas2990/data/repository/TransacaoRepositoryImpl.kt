package com.douglas2990.minhasfinancas2990.data.repository

import android.util.Log
import com.douglas2990.minhasfinancas2990.data.local.dao.TransacaoDao
import com.douglas2990.minhasfinancas2990.data.mapper.toDomain
import com.douglas2990.minhasfinancas2990.data.mapper.toEntity
import com.douglas2990.minhasfinancas2990.data.remote.api.TransacaoApi
import com.douglas2990.minhasfinancas2990.data.sync.SyncManager
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransacaoRepositoryImpl @Inject constructor(
    private val localDataSource: TransacaoDao,
    private val remoteDataSource: TransacaoApi, // Mantido para uso futuro
    private val syncManager: SyncManager
) : TransacaoRepository {

    override fun getTransacoes(): Flow<List<Transacao>> {
        return localDataSource.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransacoesPorPeriodo(mes: Int, ano: Int): Flow<List<Transacao>> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(ano, mes - 1, 1, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val inicioMillis = calendar.timeInMillis

        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.MILLISECOND, -1)
        val fimMillis = calendar.timeInMillis

        return localDataSource.getPorPeriodo(inicioMillis, fimMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun salvarTransacao(transacao: Transacao) {
        try {
            // 1. Salva localmente com isSynced = false
            // Graças ao @Index(unique = true) na sua Entity,
            // se o item já existir, o Room substituirá (REPLACE).
            val entity = transacao.toEntity(isSynced = false)
            localDataSource.insert(entity)

            // 2. Dispara a sincronização
            sincronizar()
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Falha ao persistir transação localmente: ${e.message}")
        }
    }

    override suspend fun sincronizar() {
        try {
            // O SyncManager agora deve assumir a responsabilidade de:
            // 1. Enviar itens com isSynced = false para a API
            // 2. Atualizar o flag isSynced = true após sucesso
            // 3. Buscar novos itens da API e fazer o 'upsert' (usando o índice único)
            syncManager.triggerSync()
        } catch (e: Exception) {
            Log.e("REPO_SYNC", "Falha na sincronização em background: ${e.message}")
        }
    }

    override suspend fun excluirTransacao(transacao: Transacao) {
        try {
            // Remove do local primeiro
            localDataSource.delete(transacao.toEntity(isSynced = true))

            // Opcional: Aqui você chamaria uma função específica para deletar no servidor
            // syncManager.deleteNoServidor(transacao.id)

            sincronizar()
        } catch (e: Exception) {
            Log.e("REPO_DELETE", "Falha ao excluir: ${e.message}")
        }
    }
}