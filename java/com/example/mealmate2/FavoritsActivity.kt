package com.example.mealmate2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivityFavoritsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class FavoritsActivity : BaseActivity() {

    private lateinit var binding: ActivityFavoritsBinding
    private lateinit var mealAdapter: MealsAdapter
    private lateinit var userId: String
    private var favoriteMealIds: Set<String> = emptySet()
    private val db by lazy { MealDatabase.getDatabase(this) }
    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation

    private val mealDetailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val mealId = data?.getStringExtra("MEAL_ID") ?: return@registerForActivityResult
            val isFavorite = data.getBooleanExtra("IS_FAVORITE", false)
            mealAdapter.updateMealFavoriteState(mealId, isFavorite)
            loadFavoriteMeals()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserAndFavorites()
    }

    private fun loadUserAndFavorites() {
        lifecycleScope.launch {
            val user = db.userDao().getLoggedInUser()
            userId = user?.uid ?: return@launch
            setupRecyclerView()
            loadFavoriteMeals()
        }
    }

    private fun loadFavoriteMeals() {
        lifecycleScope.launch {
            val favorites = db.favoriteDao().getFavoritesForUser(userId)
            favoriteMealIds = favorites.map { it.mealId }.toSet()
            val meals = favorites.map {
                Meal(
                    idMeal = it.mealId,
                    strMeal = it.name,
                    strMealThumb = it.image,
                    strInstructions = it.instructions,
                    strYoutube = it.youtubeLink,
                    strCategory = it.category,
                    strArea = it.area,
                    ingredientsString = it.ingredientsString
                )
            }
            mealAdapter.submitList(meals)
        }
    }

    private fun setupRecyclerView() {
        val selectedDate = intent.getStringExtra("selectedDate")

        mealAdapter = MealsAdapter(
            onMealClick = { meal -> openMealDetails(meal) },
            onAddToFavoritesClick = { meal, isFavorite ->
                lifecycleScope.launch {
                    if (isFavorite) {
                        db.favoriteDao().removeFavorite(meal.idMeal, userId)
                    } else {
                        db.favoriteDao().addFavorite(
                            FavoriteMeal(
                                mealId = meal.idMeal,
                                userId = userId,
                                name = meal.strMeal ?: "",
                                image = meal.strMealThumb ?: "",
                                instructions = meal.strInstructions ?: "",
                                youtubeLink = meal.strYoutube ?: "",
                                category = meal.strCategory ?: "",
                                area = meal.strArea ?: "",
                                ingredientsString = meal.ingredientsString ?: ""
                            )
                        )
                    }
                    loadFavoriteMeals()
                }
            },
            isFavorite = { meal -> meal.idMeal in favoriteMealIds },
            userId = userId,
            onSelectMealClick = selectedDate?.let {
                { meal -> onMealSelected(meal) }
            }
        )

        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = mealAdapter
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

    private fun openMealDetails(meal: Meal) {
        val intent = Intent(this, MealDetailsActivity::class.java).apply {
            putExtra("MEAL_ID", meal.idMeal)
            putExtra("MEAL_NAME", meal.strMeal)
            putExtra("MEAL_IMAGE", meal.strMealThumb)
            putExtra("MEAL_INSTRUCTIONS", meal.strInstructions)
            putExtra("MEAL_YOUTUBE", meal.strYoutube)
            putExtra("MEAL_CATEGORY", meal.strCategory)
            putExtra("MEAL_AREA", meal.strArea)
            putExtra("IS_FAVORITE", favoriteMealIds.contains(meal.idMeal))
            putStringArrayListExtra("MEAL_INGREDIENTS", ArrayList(meal.getIngredientsList()))
        }
        mealDetailsLauncher.launch(intent)
    }
}
