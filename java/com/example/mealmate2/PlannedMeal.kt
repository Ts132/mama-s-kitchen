package com.example.mealmate2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_meals")
data class PlannedMeals(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealId: String,
    val mealName: String,
    val mealImage: String,
    val date: String, // Format: "yyyy-MM-dd"
    val mealType: String, // "Breakfast", "Lunch", "Dinner"
    val calendarEventId: String? = null // Stores the calendar event ID
)