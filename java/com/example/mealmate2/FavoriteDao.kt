package com.example.mealmate2

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Insert
    suspend fun addFavorite(favorite: FavoriteMeal)

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    suspend fun getFavoritesForUser(userId: String): List<FavoriteMeal>

    @Query("SELECT * FROM favorites WHERE mealId = :mealId AND userId = :userId")
    suspend fun getFavoriteMeal(mealId: String, userId: String): FavoriteMeal?

    @Query("DELETE FROM favorites WHERE mealId = :mealId AND userId = :userId")
    suspend fun removeFavorite(mealId: String, userId: String)

    @Query("SELECT mealId FROM favorites WHERE userId = :userId")
    suspend fun getAllFavoritesIds(userId: String): List<String>
}

