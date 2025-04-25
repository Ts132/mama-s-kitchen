package com.example.mealmate2

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal)

    @Query("DELETE FROM meal_table WHERE idMeal = :mealId")
    suspend fun deleteById(mealId: String)

    @Query("SELECT * FROM meal_table WHERE idMeal = :mealId LIMIT 1")
    fun getMealById(mealId: String): LiveData<Meal>

    @Query("SELECT * FROM meal_table")
    fun getAllMeals(): LiveData<List<Meal>>

    @Query("SELECT * FROM meal_table WHERE isFavorite = 1")
    fun getFavoriteMeals(): LiveData<List<Meal>>

    @Query("UPDATE meal_table SET isFavorite = 1 WHERE idMeal = :mealId")
    suspend fun markAsFavorite(mealId: String)

    @Query("UPDATE meal_table SET isFavorite = 0 WHERE idMeal = :mealId")
    suspend fun unmarkAsFavorite(mealId: String)

    // ✅ أضفنا هذه الدالة لحل المشكلة
    @Query("SELECT * FROM meal_table WHERE idMeal IN (:mealIds)")
    fun getMealsByIds(mealIds: List<String>): LiveData<List<Meal>>

    @Query("SELECT * FROM favorites WHERE userId = :uid")
    suspend fun getFavoritesForUser(uid: String): List<FavoriteMeal>

}
