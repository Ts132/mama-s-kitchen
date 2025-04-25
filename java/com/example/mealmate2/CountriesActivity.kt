package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivityCountriesBinding
import com.example.mealmate2.models.Country
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CountryActivity : BaseActivity() {
    private lateinit var binding: ActivityCountriesBinding
    private lateinit var countryAdapter: CountryAdapter

    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        fetchCountries()
    }

    private fun setupRecyclerView() {
        countryAdapter = CountryAdapter { country ->
            openMealsByCountry(country)
        }
        binding.rvCountries.apply {
            layoutManager = LinearLayoutManager(this@CountryActivity)
            adapter = countryAdapter
        }
    }

    private fun fetchCountries() {
        lifecycleScope.launch {

                val response = RetrofitInstance.api.getAllCountries()
                if (response.isSuccessful) {
                    val countries = response.body()?.meals?.map { Country(it.strArea) } ?: emptyList()
                    countryAdapter.submitList(countries)
                }

        }
    }

    private fun openMealsByCountry(country: Country) {
        val intent = Intent(this, Mealc::class.java).apply {
            putExtra("COUNTRY_NAME", country.strArea)
        }
        startActivity(intent)
    }
}