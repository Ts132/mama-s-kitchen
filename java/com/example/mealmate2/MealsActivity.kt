package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivityMealsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MealsActivity : BaseActivity() {
    private lateinit var binding: ActivityMealsBinding
    private lateinit var mealsAdapter: MealsAdapter
    private lateinit var database: MealDatabase
    private lateinit var userId: String
    private var favoriteMealIds: Set<String> = emptySet()

    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        binding.tvCategoryName.text = categoryName

        database = MealDatabase.getDatabase(this)
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("userId", "") ?: ""

        loadUserAndFavorites(categoryName)
    }

    private fun loadUserAndFavorites(categoryName: String) {
        lifecycleScope.launch {
            val favorites = database.favoriteDao().getFavoritesForUser(userId)
            favoriteMealIds = favorites.map { it.mealId }.toSet()
            setupRecyclerView()
            fetchMealsByCategory(categoryName) // تعديل هنا
        }
    }


    private fun setupRecyclerView() {
        mealsAdapter = MealsAdapter(
            onMealClick = { meal -> openMealDetails(meal) },
            onAddToFavoritesClick = { meal, isFavorite ->
                lifecycleScope.launch {
                    if (isFavorite) {
                        val newFavorite = FavoriteMeal(
                            mealId = meal.idMeal,
                            name = meal.strMeal ?: "",
                            image = meal.strMealThumb ?: "",
                            userId = userId,
                            instructions = meal.strInstructions ?: "",
                            youtubeLink = meal.strYoutube ?: "",
                            category = meal.strCategory ?: "",
                            area = meal.strArea ?: "",
                            ingredientsString = meal.getIngredientsList().joinToString(", ")
                        )
                        database.favoriteDao().addFavorite(newFavorite)
                        favoriteMealIds = favoriteMealIds + meal.idMeal
                    } else {
                        database.favoriteDao().removeFavorite(meal.idMeal, userId)
                        favoriteMealIds = favoriteMealIds - meal.idMeal
                    }
                    mealsAdapter.notifyDataSetChanged()
                }
            },
            isFavorite = { meal -> favoriteMealIds.contains(meal.idMeal) },
            userId = userId
        )

        binding.rvMeals.apply {
            layoutManager = LinearLayoutManager(this@MealsActivity)
            adapter = mealsAdapter
        }
    }

    private fun fetchMealsByCategory(categoryName: String) {
        lifecycleScope.launch {
            val response = RetrofitInstance.api.getMealsByCategory(categoryName)
            if (response.isSuccessful) {
                val meals = response.body()?.meals ?: emptyList()
                mealsAdapter.submitList(meals)
            } else {
                println("API Error: ${response.errorBody()?.string()}")
            }
        }
    }


    private fun openMealDetails(meal: Meal) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(MealApiService::class.java)

        lifecycleScope.launch {
            val response = api.getMealDetails(meal.idMeal ?: "")
            if (response.isSuccessful) {
                response.body()?.meals?.firstOrNull()?.let { detailedMeal ->
                    val intent = Intent(this@MealsActivity, MealDetailsActivity::class.java).apply {
                        putExtra("MEAL_ID", detailedMeal.idMeal)
                        putExtra("MEAL_NAME", detailedMeal.strMeal)
                        putExtra("MEAL_IMAGE", detailedMeal.strMealThumb)
                        putExtra("MEAL_INSTRUCTIONS", detailedMeal.strInstructions)
                        putExtra("MEAL_YOUTUBE", detailedMeal.strYoutube)
                        putExtra("MEAL_CATEGORY", detailedMeal.strCategory)
                        putExtra("MEAL_AREA", detailedMeal.strArea)
                        putStringArrayListExtra("MEAL_INGREDIENTS", ArrayList(detailedMeal.getIngredientsList()))
                        putExtra("IS_FAVORITE", favoriteMealIds.contains(detailedMeal.idMeal))
                    }
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this@MealsActivity, "Failed to load meal details", Toast.LENGTH_LONG).show()
            }
        }
    }
}
