package com.douglas2990.minhasfinancas2990.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.douglas2990.minhasfinancas2990.data.local.dao.TransacaoDao
import com.douglas2990.minhasfinancas2990.data.local.entity.TransacaoEntity

@Database(entities = [TransacaoEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transacaoDao(): TransacaoDao
}