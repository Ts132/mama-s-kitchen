package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivitySearchBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var mealAdapter: MealsAdapter
    private lateinit var database: MealDatabase
    private lateinit var userId: String
    private var favoriteMealIds: Set<String> = emptySet()

    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = MealDatabase.getDatabase(this)
        userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("userId", "") ?: ""

        setupRecyclerView()
        loadFavorites()

        binding.btnCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        binding.btnCountries.setOnClickListener {
            startActivity(Intent(this, CountryActivity::class.java))
        }

        binding.btnSearch.setOnClickListener {
            searchMeals(binding.etSearch.text.toString())
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            favoriteMealIds = database.favoriteDao().getFavoritesForUser(userId).map { it.mealId }.toSet()
        }
    }

    private fun setupRecyclerView() {
        mealAdapter = MealsAdapter(
            onMealClick = { meal -> openMealDetails(meal) },
            onAddToFavoritesClick = { meal, isFavorite ->
                lifecycleScope.launch {
                    if (isFavorite) {
                        database.favoriteDao().removeFavorite(meal.idMeal, userId)
                    } else {
                        database.favoriteDao().addFavorite(
                            FavoriteMeal(
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
                        )
                    }
                    loadFavorites()
                    searchMeals(binding.etSearch.text.toString())
                }
            },
            isFavorite = { meal -> meal.idMeal in favoriteMealIds },
            userId = userId
        )
        binding.rvMeals.layoutManager = LinearLayoutManager(this)
        binding.rvMeals.adapter = mealAdapter
    }

    private fun searchMeals(query: String) {
        lifecycleScope.launch {
            val response = api.searchMeals(query)
            if (response.isSuccessful) {
                mealAdapter.submitList(response.body()?.meals ?: emptyList())
            } else {
                Toast.makeText(this@SearchActivity, "Failed to fetch meals", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openMealDetails(meal: Meal) {
        val intent = Intent(this, MealDetailsActivity::class.java).apply {
            putExtra("MEAL_ID", meal.idMeal)
            putExtra("MEAL_NAME", meal.strMeal)
            putExtra("MEAL_IMAGE", meal.strMealThumb)
            putExtra("MEAL_INSTRUCTIONS", meal.strInstructions)
            putExtra("MEAL_YOUTUBE", meal.strYoutube)
            putExtra("MEAL_CATEGORY", meal.strCategory)
            putExtra("MEAL_AREA", meal.strArea)
            putStringArrayListExtra("MEAL_INGREDIENTS", ArrayList(meal.getIngredientsList()))
        }
        startActivity(intent)
    }
    private fun onMealSelected(meal: Meal) {
        val selectedDate = intent.getStringExtra("selectedDate") ?: return

        val resultIntent = Intent().apply {
            putExtra("mealId", meal.idMeal)
            putExtra("mealName", meal.strMeal ?: "")
            putExtra("mealType", meal.strCategory ?: "غير محدد")
            putExtra("selectedDate", selectedDate)
            putExtra("mealImage", meal.strMealThumb) // << مهم جدًا

        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}
