package com.douglas2990.minhasfinancas2990.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transacoes",
    indices = [Index(value = ["titulo", "valor", "data"], unique = true)] // Impede duplicatas exatas
)
data class TransacaoEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val remoteId: String? = null,
    val titulo: String,
    val valor: Double,
    val data: Long,
    val tipo: String,
    val categoria: String,
    val metodo: String,
    val sincronizado: Boolean = false
)