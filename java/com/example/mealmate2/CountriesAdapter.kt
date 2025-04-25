package com.example.mealmate2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.databinding.ItemCountryBinding
import com.example.mealmate2.models.Country

class CountryAdapter(
    private val onItemClick: (Country) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    private var countries: List<Country> = emptyList()

    fun submitList(newCountries: List<Country>) {
        countries = newCountries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(countries[position], onItemClick)
    }

    override fun getItemCount() = countries.size

    inner class CountryViewHolder(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country, onClick: (Country) -> Unit) {
            binding.btnCountry.text = country.strArea
            binding.btnCountry.setOnClickListener { onClick(country) }
        }
    }
}