package com.example.mealmate2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mealmate2.databinding.ActivityMealDetailsBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.launch

class MealDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealDetailsBinding
    private lateinit var userId: String
    private var isFavorite: Boolean = false
    private lateinit var mealId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isGuest = sharedPreferences.getBoolean("isGuest", false)

        // إخفاء زر المفضلة لو المستخدم ضيف
        if (isGuest) {
            binding.btnAddToFavorite.visibility = View.GONE
        }
        mealId = intent.getStringExtra("MEAL_ID") ?: ""
        val mealName = intent.getStringExtra("MEAL_NAME") ?: "No Name"
        val mealImage = intent.getStringExtra("MEAL_IMAGE")
        val mealInstructions = intent.getStringExtra("MEAL_INSTRUCTIONS") ?: "No Instructions"
        val mealYoutube = intent.getStringExtra("MEAL_YOUTUBE") ?: ""
        val mealCategory = intent.getStringExtra("MEAL_CATEGORY") ?: "No Category"
        val mealArea = intent.getStringExtra("MEAL_AREA") ?: "No Area"
        val mealIngredients = intent.getStringArrayListExtra("MEAL_INGREDIENTS") ?: arrayListOf("No Ingredients")
        isFavorite = intent.getBooleanExtra("IS_FAVORITE", false)
        updateFavoriteButtonText()

        binding.tvMealTitle.text = mealName
        binding.tvMealCategory.text = mealCategory
        binding.tvMealArea.text = mealArea
        binding.tvMealIngredients.text = mealIngredients.joinToString("\n")
        binding.tvMealInstructions.text = mealInstructions
        binding.tvMealInstructions.movementMethod = ScrollingMovementMethod()

        Glide.with(this).load(mealImage).into(binding.ivMealImage)

        if (mealYoutube.isNotEmpty()) {
            val videoId = extractYoutubeVideoId(mealYoutube)
            lifecycle.addObserver(binding.youtubePlayerView)
            binding.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0f)
                }
            })
        }

        loadUserInfo(mealId)

        binding.btnAddToFavorite.setOnClickListener {
            val meal = Meal(
                idMeal = mealId,
                strMeal = mealName,
                strMealThumb = mealImage ?: "",
                strCategory = mealCategory,
                strArea = mealArea,
                ingredient1 = mealIngredients.firstOrNull(),
                strInstructions = mealInstructions,
                strYoutube = mealYoutube
            )

            if (isFavorite) {
                removeFromFavorites(meal)
            } else {
                addToFavorites(meal)
            }
        }
    }

    private fun extractYoutubeVideoId(youtubeUrl: String): String {
        return Uri.parse(youtubeUrl).getQueryParameter("v") ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayerView.release()
    }

    private fun loadUserInfo(mealId: String) {
        lifecycleScope.launch {
            val userDao = MealDatabase.getDatabase(applicationContext).userDao()
            val user = userDao.getLoggedInUser()
            user?.let {
                userId = it.uid
                checkIfMealIsFavorite(mealId)
            }
        }
    }

    private fun checkIfMealIsFavorite(mealId: String) {
        lifecycleScope.launch {
            val favoriteDao = MealDatabase.getDatabase(applicationContext).favoriteDao()
            val favoriteMeal = favoriteDao.getFavoriteMeal(userId, mealId)
            isFavorite = favoriteMeal != null
            updateFavoriteButtonText()
        }
    }

    private fun updateFavoriteButtonText() {
        binding.btnAddToFavorite.text = if (isFavorite) "Remove from Favorites" else "Add to Favorites"
    }

    private fun addToFavorites(meal: Meal) {
        lifecycleScope.launch {
            val favoriteDao = MealDatabase.getDatabase(applicationContext).favoriteDao()
            val favoriteEntity = FavoriteMeal.fromMeal(meal, userId)
            favoriteDao.addFavorite(favoriteEntity)

            isFavorite = true
            updateFavoriteButtonText()

            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("MEAL_ID", meal.idMeal)
                putExtra("IS_FAVORITE", true)
            })
            Toast.makeText(this@MealDetailsActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromFavorites(meal: Meal) {
        lifecycleScope.launch {
            val favoriteDao = MealDatabase.getDatabase(applicationContext).favoriteDao()
            favoriteDao.removeFavorite(meal.idMeal, userId)

            isFavorite = false
            updateFavoriteButtonText()

            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("MEAL_ID", meal.idMeal)
                putExtra("IS_FAVORITE", false)
            })
            Toast.makeText(this@MealDetailsActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
        }
    }
}
