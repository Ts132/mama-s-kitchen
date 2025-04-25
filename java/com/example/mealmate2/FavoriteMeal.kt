package com.example.mealmate2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteMeal(
    @PrimaryKey val mealId: String,
    val name: String,
    val image: String,
    val userId: String,
    val instructions: String,
    val youtubeLink: String,
    val category: String,
    val area: String,
    // نخزن كل المكونات في حقل واحد، مفصول بفواصل
    val ingredientsString: String = ""
)
{
    companion object {
        fun fromMeal(meal: Meal, userId: String): FavoriteMeal {
            return FavoriteMeal(
                mealId = meal.idMeal,
                userId = userId,
                name = meal.strMeal,
                image = meal.strMealThumb,
                instructions = meal.strInstructions,
                youtubeLink = meal.strYoutube,
                category = meal.strCategory,
                area = meal.strArea,
                ingredientsString = meal.getIngredientsList().joinToString(", ")
            )
        }
    }
}
