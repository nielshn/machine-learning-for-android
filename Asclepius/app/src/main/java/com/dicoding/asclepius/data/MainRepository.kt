package com.dicoding.asclepius.data

import com.dicoding.asclepius.data.local.entity.HistoryEntity
import com.dicoding.asclepius.data.local.room.HistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainRepository private constructor(
    private val historyDao: HistoryDao
) {
    suspend fun insertHistory(history: HistoryEntity) {
        return withContext(Dispatchers.IO) {
            historyDao.insertEvents(history)
        }
    }

    fun getAllHistory() = historyDao.getAllHistory()

    suspend fun deleteAllHistory() {
        return withContext(Dispatchers.IO) {
            historyDao.deleteAll()
        }
    }

    companion object {
        @Volatile
        private var instance: MainRepository? = null

        fun getInstance(
            historyDao: HistoryDao
        ): MainRepository = instance ?: synchronized(this) {
            instance ?: MainRepository(historyDao)
        }.also { instance = it }
    }
}