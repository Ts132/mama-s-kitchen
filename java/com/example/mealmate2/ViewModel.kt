package com.example.mealmate2

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MealViewModel(private val repository: MealRepository) : ViewModel() {
    fun getPlannedMealsForDate(date: String): LiveData<List<PlannedMeals>> {
        return repository.getPlannedMealsForDate(date)
    }

    fun insertPlannedMeal(plannedMeal: PlannedMeals) {
        viewModelScope.launch {
            repository.insertPlannedMeal(plannedMeal)
        }
    }

    fun deletePlannedMeal(mealId: String, date: String) {
        viewModelScope.launch {
            repository.deletePlannedMeal(mealId, date)
        }
    }

    fun clearOldPlans() {
        viewModelScope.launch {
            repository.clearOldPlans()
        }
    }

    fun updatePlannedMealWithEventId(id: Int, eventId: String) {
        viewModelScope.launch {
            repository.updatePlannedMealWithEventId(id, eventId)
        }
    }
}