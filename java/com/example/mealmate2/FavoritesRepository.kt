package com.example.mealmate2.repository

import com.example.mealmate2.FavoriteDao
import com.example.mealmate2.FavoriteMeal

class FavoritesRepository(private val favoriteDao: FavoriteDao) {

    suspend fun addToFavorites(meal: FavoriteMeal) {
        favoriteDao.addFavorite(meal)
    }

    suspend fun removeFromFavorites(mealId: String, userId: String) {
        favoriteDao.removeFavorite(mealId, userId)
    }

    suspend fun getUserFavorites(userId: String): List<FavoriteMeal> {
        return favoriteDao.getFavoritesForUser(userId)
    }
}
