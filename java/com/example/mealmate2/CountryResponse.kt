package com.example.mealmate2.models

import com.google.gson.annotations.SerializedName

data class CountryResponse(
    @SerializedName("meals") val countries: List<Country>
)


