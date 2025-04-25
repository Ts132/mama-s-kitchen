package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mealmate2.CategoriesAdapter
import com.example.mealmate2.MealApiService
import com.example.mealmate2.databinding.ActivityCategoriesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategoriesActivity : BaseActivity(){
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var categoriesAdapter: CategoriesAdapter

    // Overriding the bottomNavigationView from BaseActivity
    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideIfGuest(R.id.btnAddToFavorite)

        setupRecyclerView()
        fetchCategories()

    }



    private fun setupRecyclerView() {
        categoriesAdapter = CategoriesAdapter { category ->
            val intent = Intent(this, MealsActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category.strCategory) // إرسال اسم الـ Category
            startActivity(intent)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@CategoriesActivity, 2)
            adapter = categoriesAdapter
        }
    }


    private fun fetchCategories() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(MealApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = api.getCategories()
                if (response.isSuccessful) {
                    val categoriesList = response.body()?.categories
                    if (categoriesList != null) {
                        categoriesAdapter.submitList(categoriesList)
                    } else {
                        Toast.makeText(this@CategoriesActivity, "No categories found", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@CategoriesActivity, "Failed: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CategoriesActivity, "Exception: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }}
