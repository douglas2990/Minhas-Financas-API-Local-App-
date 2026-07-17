package com.douglas2990.minhasfinancas2990.data.repository

import android.content.Context
import android.util.Log
import com.douglas2990.minhasfinancas2990.data.local.dao.TransacaoDao
import com.douglas2990.minhasfinancas2990.data.mapper.toDomain
import com.douglas2990.minhasfinancas2990.data.mapper.toDto
import com.douglas2990.minhasfinancas2990.data.mapper.toEntity
import com.douglas2990.minhasfinancas2990.data.remote.api.TransacaoApi
import com.douglas2990.minhasfinancas2990.data.sync.SyncManager
import com.douglas2990.minhasfinancas2990.data.sync.WorkManagerScheduler
import com.douglas2990.minhasfinancas2990.domain.model.Transacao
import com.douglas2990.minhasfinancas2990.domain.repository.TransacaoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransacaoRepositoryImpl @Inject constructor(
    private val localDataSource: TransacaoDao,
    private val remoteDataSource: TransacaoApi,
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
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
            // 1. Sempre salva localmente primeiro (offline-first)
            val entity = transacao.toEntity(isSynced = false)
            localDataSource.insert(entity)
            Log.d("REPO", "Salvo localmente: ${transacao.titulo} | sincronizado=false")

            // 2. Agenda sync via WorkManager (só executa quando houver rede)
            WorkManagerScheduler.agendarSync(context)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Falha ao salvar localmente: ${e.message}")
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
            localDataSource.delete(transacao.toEntity(isSynced = true))
            sincronizar()
        } catch (e: Exception) {
            Log.e("REPO_DELETE", "Falha ao excluir: ${e.message}")
        }
    }

    /** Remove do Room sem tocar na API — usado na tela de pendentes */
    override suspend fun excluirLocal(transacao: Transacao) {
        try {
            localDataSource.delete(transacao.toEntity(isSynced = false))
            Log.d("REPO", "Excluído localmente sem enviar à API: ${transacao.titulo}")
        } catch (e: Exception) {
            Log.e("REPO_DELETE_LOCAL", "Falha ao excluir localmente: ${e.message}")
        }
    }

    /** Envia UM item específico para a API e atualiza sincronizado = true no Room */
    override suspend fun sincronizarUm(transacao: Transacao) {
        try {
            val dto = transacao.toEntity(isSynced = false).toDto()
            val response = remoteDataSource.enviarTransacao(dto)
            if (response.isSuccessful) {
                // Marca como sincronizado no Room
                val entityAtualizada = transacao.toEntity(isSynced = true)
                localDataSource.update(entityAtualizada)
                Log.d("REPO_SYNC_UM", "Enviado e marcado como sincronizado: ${transacao.titulo}")
            } else {
                val erro = response.errorBody()?.string()
                throw Exception("API retornou erro ${response.code()}: $erro")
            }
        } catch (e: Exception) {
            Log.e("REPO_SYNC_UM", "Falha ao sincronizar item: ${e.message}", e)
            throw e
        }
    }

    override fun getPendentesCount(): Flow<Int> = localDataSource.contarNaoSincronizadas()

    override suspend fun buscarCategorias(): List<com.douglas2990.minhasfinancas2990.domain.model.CategoriaDto> {
        return try {
            Log.d("REPO_CATEGORIAS", "Iniciando busca de categorias na API...")
            val resultado = remoteDataSource.buscarCategorias()
            Log.d("REPO_CATEGORIAS", "Sucesso! ${resultado.size} categorias recebidas: $resultado")
            resultado
        } catch (e: Exception) {
            Log.e("REPO_CATEGORIAS", "ERRO ao buscar categorias: ${e.javaClass.simpleName} - ${e.message}")
            Log.e("REPO_CATEGORIAS", "Stack trace:", e)
            emptyList()
        }
    }

    override suspend fun buscarMetodos(): List<com.douglas2990.minhasfinancas2990.domain.model.MetodoPagamentoDto> {
        return try {
            Log.d("REPO_METODOS", "Iniciando busca de métodos na API...")
            val resultado = remoteDataSource.buscarMetodos()
            Log.d("REPO_METODOS", "Sucesso! ${resultado.size} métodos recebidos: $resultado")
            resultado
        } catch (e: Exception) {
            Log.e("REPO_METODOS", "ERRO ao buscar métodos: ${e.javaClass.simpleName} - ${e.message}")
            Log.e("REPO_METODOS", "Stack trace:", e)
            emptyList()
        }
    }
}