package com.example.mealmate2

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.Locale

@Dao
interface PlannedMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plannedMeal: PlannedMeals)

    @Query("SELECT * FROM planned_meals WHERE date = :date ORDER BY mealType")
    fun getMealsForDate(date: String): LiveData<List<PlannedMeals>>

    @Query("DELETE FROM planned_meals WHERE mealId = :mealId AND date = :date")
    suspend fun deleteMealFromPlan(mealId: String, date: String)

    @Query("DELETE FROM planned_meals WHERE date < :weekOldDate")
    suspend fun clearOldPlans(weekOldDate: String)

    @Query("UPDATE planned_meals SET calendarEventId = :eventId WHERE id = :id")
    suspend fun updateEventId(id: Int, eventId: String)

    companion object {
        fun getWeekOldDate(): String {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        }
    }
}