package com.example.mealmate2

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealmate2.databinding.ItemPlannedMealBinding

class PlannedMealsAdapter(
    private val onDeleteClick: (PlannedMeals) -> Unit,
    private val onAddToCalendarClick: (PlannedMeals) -> Unit
) : RecyclerView.Adapter<PlannedMealsAdapter.MealViewHolder>() {

    private var plannedMealsList: List<PlannedMeals> = emptyList()

    fun submitList(plannedMeals: List<PlannedMeals>) {
        plannedMealsList = plannedMeals
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemPlannedMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(plannedMealsList[position])
    }

    override fun getItemCount(): Int = plannedMealsList.size

    inner class MealViewHolder(private val binding: ItemPlannedMealBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plannedMeal: PlannedMeals) {
            binding.apply {
                textMealName.text = plannedMeal.mealName
                textMealDate.text = "Date: ${plannedMeal.date}"
                textMealType.text = "Type: ${plannedMeal.mealType}"

                // Improved image loading with error handling
                if (plannedMeal.mealImage.isNotEmpty()) {
                    Glide.with(imageMeal.context)
                        .load(plannedMeal.mealImage)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error) // Add an error placeholder
                        .into(imageMeal)
                } else {
                    imageMeal.setImageResource(R.drawable.placeholder_image)
                }

                buttonDeleteMeal.setOnClickListener {
                    onDeleteClick(plannedMeal)
                }

                buttonAddToCalendar.setOnClickListener {
                    onAddToCalendarClick(plannedMeal)
                }

                buttonAddToCalendar.isEnabled = plannedMeal.calendarEventId.isNullOrEmpty()
            }
        }
    }}