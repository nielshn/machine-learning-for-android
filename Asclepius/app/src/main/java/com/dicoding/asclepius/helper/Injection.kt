package com.dicoding.asclepius.helper

import android.content.Context
import com.dicoding.asclepius.data.MainRepository
import com.dicoding.asclepius.data.local.room.HistoryDatabase

object Injection {
    fun provideRepository(context: Context) : MainRepository {
        val db = HistoryDatabase.getDatabase(context)
        val dao = db.historyDao()
        return MainRepository.getInstance(dao)
    }
}