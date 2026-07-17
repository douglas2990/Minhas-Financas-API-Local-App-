package com.douglas2990.minhasfinancas2990.data.local.dao


import androidx.room.*
import com.douglas2990.minhasfinancas2990.data.local.entity.TransacaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransacaoDao {
    @Query("SELECT * FROM transacoes ORDER BY data DESC")
    fun getAll(): Flow<List<TransacaoEntity>>

    // Filtro por período: usado para o filtro de mês/ano na tela principal
    @Query("SELECT * FROM transacoes WHERE data BETWEEN :inicioMillis AND :fimMillis ORDER BY data DESC")
    fun getPorPeriodo(inicioMillis: Long, fimMillis: Long): Flow<List<TransacaoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transacao: TransacaoEntity)

    @Delete
    suspend fun delete(transacao: TransacaoEntity)

    // Útil para o SyncManager: buscar só o que não foi enviado ainda
    @Query("SELECT * FROM transacoes WHERE sincronizado = 0")
    suspend fun getNaoSincronizadas(): List<TransacaoEntity>

    // Reativo: emite sempre que a contagem de pendentes mudar (para o banner da UI)
    @Query("SELECT COUNT(*) FROM transacoes WHERE sincronizado = 0")
    fun contarNaoSincronizadas(): Flow<Int>

    @Update
    suspend fun update(transacao: TransacaoEntity)
}