package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var mealAdapter: MealsAdapter
    private var userId: String = ""
    private var favoriteMealIds: Set<String> = emptySet()
    private lateinit var googleSignInClient: GoogleSignInClient

    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        loadUserInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val isGuest = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getBoolean("isGuest", false)

        if (isGuest) {
            menu?.findItem(R.id.action_logout)?.isVisible = false
        }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserInfo() {
        lifecycleScope.launch {
            val userDao = MealDatabase.getDatabase(applicationContext).userDao()
            val user = userDao.getLoggedInUser()

            user?.let {
                Log.d("UserInfo", "User: ${it.name}")
                userId = it.uid
                binding.tvUserName.text = "Hi ${it.name}👋 "
            } ?: run {
                binding.tvUserName.text = "Mama's Kitchen"
                userId = ""
            }

            setupRecyclerView()
            fetchFavoritesAndMeals()
        }
    }

    private fun setupRecyclerView() {
        mealAdapter = MealsAdapter(
            onMealClick = { meal -> openMealDetails(meal) },
            onAddToFavoritesClick = { meal, newState ->
                lifecycleScope.launch {
                    val favoriteDao = MealDatabase.getDatabase(applicationContext).favoriteDao()
                    if (newState) {
                        favoriteDao.addFavorite(
                            FavoriteMeal(
                                meal.idMeal,
                                meal.strMeal,
                                meal.strMealThumb,
                                userId,
                                meal.strInstructions,
                                meal.strYoutube,
                                meal.strCategory,
                                meal.strArea,
                                ingredientsString = meal.getIngredientsList().joinToString(", ")
                            )
                        )
                        favoriteMealIds = favoriteMealIds + meal.idMeal
                    } else {
                        favoriteDao.removeFavorite(meal.idMeal, userId)
                        favoriteMealIds = favoriteMealIds - meal.idMeal
                    }
                    mealAdapter.updateMealFavoriteState(meal.idMeal, newState)
                }
            },
            isFavorite = { meal -> favoriteMealIds.contains(meal.idMeal) },
            userId = userId
        )

        binding.rvMeals.apply {
            adapter = mealAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }
    }

    private fun fetchFavoritesAndMeals() {
        lifecycleScope.launch {
            val favoriteDao = MealDatabase.getDatabase(applicationContext).favoriteDao()
            favoriteMealIds = favoriteDao.getAllFavoritesIds(userId).toSet()
            fetchMeals()
        }
    }

    private fun fetchMeals() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(MealApiService::class.java)

        lifecycleScope.launch {
            val mealsList = mutableListOf<Meal>()
            repeat(5) {
                val response = api.getMealOfTheDay()
                if (response.isSuccessful) {
                    response.body()?.meals?.let { mealsList.addAll(it) }
                }
            }
            mealAdapter.submitList(mealsList)
        }
    }

    private fun openMealDetails(meal: Meal) {
        val ingredientsList = meal.getIngredientsList()
        val intent = Intent(this, MealDetailsActivity::class.java).apply {
            putExtra("MEAL_ID", meal.idMeal)
            putExtra("MEAL_NAME", meal.strMeal)
            putExtra("MEAL_IMAGE", meal.strMealThumb)
            putExtra("MEAL_INSTRUCTIONS", meal.strInstructions)
            putExtra("MEAL_YOUTUBE", meal.strYoutube)
            putExtra("MEAL_CATEGORY", meal.strCategory)
            putExtra("MEAL_AREA", meal.strArea)
            putStringArrayListExtra("MEAL_INGREDIENTS", ArrayList(ingredientsList))
        }
        startActivity(intent)
    }

    private fun showLogoutConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("تأكيد تسجيل الخروج")
            .setMessage("هل أنت متأكد أنك تريد تسجيل الخروج؟")
            .setPositiveButton("نعم") { _, _ -> performLogout() }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun performLogout() {
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("isLoggedIn", false).apply()

        lifecycleScope.launch {
            val userDao = MealDatabase.getDatabase(applicationContext).userDao()
            userDao.clearAllUsers()
        }

        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                val intent = Intent(this, Hallo::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
