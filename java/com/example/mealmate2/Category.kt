package com.example.mealmate2.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("idCategory") val idCategory: String,
    @SerializedName("strCategory") val strCategory: String,
    @SerializedName("strCategoryThumb") val strCategoryThumb: String
)