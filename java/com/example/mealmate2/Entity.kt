package com.example.mealmate2

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "meal_table")
data class Meal(
    @PrimaryKey @SerializedName("idMeal") val idMeal: String = "",
    @SerializedName("strMeal") val strMeal: String = "Unknown Meal",
    @SerializedName("strCategory") val strCategory: String = "Unknown Category",
    @SerializedName("strArea") val strArea: String = "Unknown Area",
    @SerializedName("strMealThumb") val strMealThumb: String = "",
    @SerializedName("strInstructions") val strInstructions: String = "No instructions available.",

    @SerializedName("strYoutube") val strYoutube: String = "",
    @SerializedName("strIngredient1") val ingredient1: String? = null,
    @SerializedName("strIngredient2") val ingredient2: String? = null,
    @SerializedName("strIngredient3") val ingredient3: String? = null,
    @SerializedName("strIngredient4") val ingredient4: String? = null,
    @SerializedName("strIngredient5") val ingredient5: String? = null,
    @SerializedName("strIngredient6") val ingredient6: String? = null,
    @SerializedName("strIngredient7") val ingredient7: String? = null,
    @SerializedName("strIngredient8") val ingredient8: String? = null,
    @SerializedName("strIngredient9") val ingredient9: String? = null,
    @SerializedName("strIngredient10") val ingredient10: String? = null,
    @SerializedName("strIngredient11") val ingredient11: String? = null,
    @SerializedName("strIngredient12") val ingredient12: String? = null,
    @SerializedName("strIngredient13") val ingredient13: String? = null,
    @SerializedName("strIngredient14") val ingredient14: String? = null,
    @SerializedName("strIngredient15") val ingredient15: String? = null,
    @SerializedName("strIngredient16") val ingredient16: String? = null,
    @SerializedName("strIngredient17") val ingredient17: String? = null,
    @SerializedName("strIngredient18") val ingredient18: String? = null,
    @SerializedName("strIngredient19") val ingredient19: String? = null,
    @SerializedName("strIngredient20") val ingredient20: String? = null,

    // measures
    @SerializedName("strMeasure1") val measure1: String? = null,
    @SerializedName("strMeasure2") val measure2: String? = null,
    @SerializedName("strMeasure3") val measure3: String? = null,
    @SerializedName("strMeasure4") val measure4: String? = null,
    @SerializedName("strMeasure5") val measure5: String? = null,
    @SerializedName("strMeasure6") val measure6: String? = null,
    @SerializedName("strMeasure7") val measure7: String? = null,
    @SerializedName("strMeasure8") val measure8: String? = null,
    @SerializedName("strMeasure9") val measure9: String? = null,
    @SerializedName("strMeasure10") val measure10: String? = null,
    @SerializedName("strMeasure11") val measure11: String? = null,
    @SerializedName("strMeasure12") val measure12: String? = null,
    @SerializedName("strMeasure13") val measure13: String? = null,
    @SerializedName("strMeasure14") val measure14: String? = null,
    @SerializedName("strMeasure15") val measure15: String? = null,
    @SerializedName("strMeasure16") val measure16: String? = null,
    @SerializedName("strMeasure17") val measure17: String? = null,
    @SerializedName("strMeasure18") val measure18: String? = null,
    @SerializedName("strMeasure19") val measure19: String? = null,
    @SerializedName("strMeasure20") val measure20: String? = null,

    val isFavorite: Boolean = false,

    // ✅ جديد: لعرض الوجبة من قاعدة البيانات المحلية
    val ingredientsString: String? = null
) {
    fun getIngredientsList(): List<String> {
        // لو عندنا ingredientsString (وجبة محفوظة أوفلاين)، نستخدمه
        ingredientsString?.let {
            return it.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }

        // لو مش موجود، نستخدم الـ ingredient + measure
        val ingredients = listOf(
            ingredient1, ingredient2, ingredient3, ingredient4, ingredient5,
            ingredient6, ingredient7, ingredient8, ingredient9, ingredient10,
            ingredient11, ingredient12, ingredient13, ingredient14, ingredient15,
            ingredient16, ingredient17, ingredient18, ingredient19, ingredient20
        )
        val measures = listOf(
            measure1, measure2, measure3, measure4, measure5,
            measure6, measure7, measure8, measure9, measure10,
            measure11, measure12, measure13, measure14, measure15,
            measure16, measure17, measure18, measure19, measure20
        )

        return ingredients.zip(measures).mapNotNull { (ing, meas) ->
            ing?.takeIf { it.isNotBlank() }?.let {
                if (!meas.isNullOrBlank()) "$it – ${meas.trim()}" else it
            }
        }
    }
}
