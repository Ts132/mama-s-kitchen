package com.example.mealmate2

import com.example.mealmate2.models.CategoryResponse
import com.example.mealmate2.models.CountryResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") mealId: String): Response<MealResponse>
    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealResponse>
    @GET("random.php")
    suspend fun getMealOfTheDay(): Response<MealResponse>
    @GET("categories.php")
    suspend fun getCategories(): Response<CategoryResponse>
    @GET("filter.php")
    suspend fun getMealsByCountry(@Query("a") countryName: String): Response<MealResponse>

    @GET("list.php?a=list")
    suspend fun getAllCountries(): Response<MealResponse>
    @GET("json/v1/1/search.php?s=")
    suspend fun getAllMeals(): Response<MealResponse>
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): Response<MealResponse>

}