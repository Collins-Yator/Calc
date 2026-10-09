package com.example.calculator.data

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val history: Flow<List<CalculationItem>> = dao.getAllHistory()

    suspend fun saveCalculation(item: CalculationItem): Long = dao.insert(item)

    suspend fun deleteCalculation(id: Long) = dao.deleteById(id)

    suspend fun clearAllHistory() = dao.clearAll()

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = dao.updateFavorite(id, isFavorite)
}
