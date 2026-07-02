package com.douglas2990.minhasfinancas2990.data.sync

import android.util.Log
import com.douglas2990.minhasfinancas2990.data.local.dao.TransacaoDao
import com.douglas2990.minhasfinancas2990.data.mapper.toDomain
import com.douglas2990.minhasfinancas2990.data.mapper.toDto
import com.douglas2990.minhasfinancas2990.data.mapper.toEntity
import com.douglas2990.minhasfinancas2990.data.remote.api.TransacaoApi
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val localDao: TransacaoDao,
    private val api: TransacaoApi
) {
    suspend fun triggerSync() {
        Log.d("SYNC_DEBUG", "Iniciando triggerSync...")

        try {
            // A. DOWNLOAD
            val transacoesRemotas = api.buscarTransacoes()
            Log.d("SYNC_DEBUG", "API retornou ${transacoesRemotas.size} transações.")

            transacoesRemotas.forEach { dto ->
                try {
                    val entity = dto.toDomain().toEntity(isSynced = true)
                    Log.d("SYNC_DEBUG", "Tentando inserir: ${entity.titulo} | Valor: ${entity.valor}")
                    localDao.insert(entity)
                    Log.d("SYNC_DEBUG", "Sucesso ao salvar/atualizar: ${entity.titulo}")
                } catch (e: Exception) {
                    Log.e("SYNC_ERRO", "Falha ao inserir item: ${e.message}")
                }
            }

            // B. UPLOAD
            val pendentes = localDao.getNaoSincronizadas()
            Log.d("SYNC_DEBUG", "Pendentes para enviar: ${pendentes.size}")

            pendentes.forEach { entity ->
                try {
                    val response = api.enviarTransacao(entity.toDto())
                    if (response.isSuccessful) {
                        localDao.update(entity.copy(sincronizado = true))
                        Log.d("SYNC_DEBUG", "Enviado com sucesso: ${entity.titulo}")
                    } else {
                        // ADICIONADO: Captura o corpo do erro da API para sabermos o motivo
                        val errorBody = response.errorBody()?.string()
                        Log.e("SYNC_ERRO", "Erro HTTP ${response.code()}: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e("SYNC_ERRO", "Falha de rede ao enviar: ${entity.titulo}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SYNC_ERRO", "Erro crítico no SyncManager: ", e)
        }
    }
}