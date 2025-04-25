package com.example.mealmate2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.mealmate2.databinding.ItemMealBinding

class MealsAdapter(
    private val onMealClick: (Meal) -> Unit,
    private val onAddToFavoritesClick: (Meal, Boolean) -> Unit,
    private val isFavorite: (Meal) -> Boolean,
    private val userId: String,
    private val onSelectMealClick: ((Meal) -> Unit)? = null // Callback اختياري لتحديد وجبة للتقويم
) : RecyclerView.Adapter<MealsAdapter.MealViewHolder>() {

    private var meals: List<Meal> = emptyList()
    private var favoriteMealIds: Set<String> = emptySet()

    fun updateFavoriteMeals(favorites: Set<String>) {
        favoriteMealIds = favorites
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
    }

    override fun getItemCount(): Int = meals.size

    inner class MealViewHolder(private val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(meal: Meal) {
            val context = binding.root.context
            val isGuest = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getBoolean("isGuest", false)

            binding.tvMealName.text = meal.strMeal
            binding.ivMealImage.load(meal.strMealThumb)

            // إخفاء زر المفضلة لو المستخدم ضيف
            if (isGuest) {
                binding.btnAddToFavorite.visibility = View.GONE
            } else {
                binding.btnAddToFavorite.visibility = View.VISIBLE
                val isMealFavorite = favoriteMealIds.contains(meal.idMeal)
                binding.btnAddToFavorite.text = if (isFavorite(meal)) "Remove from Favorite" else "Add to Favorite"

                binding.btnAddToFavorite.setOnClickListener {
                    val newFavoriteState = !isMealFavorite
                    onAddToFavoritesClick(meal, newFavoriteState)
                }
            }

            // فتح التفاصيل أو اختيار للجدول حسب نية المستخدم
            binding.root.setOnClickListener {
                if (onSelectMealClick != null) {
                    onSelectMealClick.invoke(meal)
                } else {
                    onMealClick(meal)
                }
            }
        }
    }

    fun updateMealFavoriteState(mealId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteMealIds = favoriteMealIds + mealId
        } else {
            favoriteMealIds = favoriteMealIds - mealId
        }
        notifyDataSetChanged()
    }

    fun submitList(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }
}
