package com.example.mealmate2

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MealRepository(application: Application) {

    private val mealDao: MealDao
    private val plannedMealDao: PlannedMealDao

    init {
        val database = MealDatabase.getDatabase(application)
        mealDao = database.mealDao()
        plannedMealDao = database.plannedMealDao()
    }

    val allMeals: LiveData<List<Meal>> = mealDao.getAllMeals()
    val favoriteMeals: LiveData<List<Meal>> = mealDao.getFavoriteMeals()

    suspend fun insert(meal: Meal) {
        mealDao.insert(meal)
    }

    suspend fun deleteById(mealId: String) {
        mealDao.deleteById(mealId)
    }

    fun getMealById(mealId: String): LiveData<Meal> {
        return mealDao.getMealById(mealId)
    }

    suspend fun deletePlannedMeal(mealId: String, date: String) {
        withContext(Dispatchers.IO) {
            plannedMealDao.deleteMealFromPlan(mealId, date)
        }
    }

    fun getPlannedMealsForDate(date: String): LiveData<List<PlannedMeals>> {
        return plannedMealDao.getMealsForDate(date)
    }

    fun getMealsByIds(mealIds: List<String>): LiveData<List<Meal>> {
        return mealDao.getMealsByIds(mealIds)
    }

    suspend fun insertPlannedMeal(plannedMeal: PlannedMeals) {
        withContext(Dispatchers.IO) {
            plannedMealDao.insert(plannedMeal)
        }
    }

    suspend fun markAsFavorite(mealId: String) {
        mealDao.markAsFavorite(mealId)
    }
    suspend fun clearOldPlans() {
        val weekOldDate = PlannedMealDao.getWeekOldDate()
        plannedMealDao.clearOldPlans(weekOldDate)
    }
    suspend fun unmarkAsFavorite(mealId: String) {
        mealDao.unmarkAsFavorite(mealId)
    }

    suspend fun updatePlannedMealWithEventId(id: Int, eventId: String) {
        plannedMealDao.updateEventId(id, eventId)
    }

}
