package com.example.mealmate2

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmate2.databinding.ActivityCalendarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity :  BaseActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var plannedMealsAdapter: PlannedMealsAdapter
    private lateinit var mealViewModel: MealViewModel
    private var selectedDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    override val bottomNavigationView: BottomNavigationView
        get() = binding.bottomNavigation
    companion object {
        private const val CALENDAR_PERMISSION_REQUEST = 1001
        private const val REQUEST_CODE_FAVORITES = 1002
        private const val REQUEST_CODE_SEARCH = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = MealRepository(application)
        mealViewModel = ViewModelProvider(this, MealViewModelFactory(repository))[MealViewModel::class.java]

        setupRecyclerView()
        setupCalendarView()
        setupAddMealButton()
        setupWeekResetCheck()
    }

    private fun setupRecyclerView() {
        plannedMealsAdapter = PlannedMealsAdapter(
            onDeleteClick = { plannedMeal ->
                mealViewModel.deletePlannedMeal(plannedMeal.mealId, plannedMeal.date)
                removeFromDeviceCalendar(plannedMeal)
                fetchPlannedMealsForDate(selectedDate)
            },
            onAddToCalendarClick = { plannedMeal ->
                addToDeviceCalendar(plannedMeal)
            }
        )

        binding.rvMeals.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = plannedMealsAdapter
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            fetchPlannedMealsForDate(selectedDate)
        }
        selectedDate = dateFormat.format(Date())
        fetchPlannedMealsForDate(selectedDate)
    }

    private fun setupAddMealButton() {
        binding.fabAddMeal.setOnClickListener { view ->
            showAddMealPopup(view)
        }
    }

    private fun showAddMealPopup(view: View) {
        val popupMenu = android.widget.PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.add_meal_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_add_saved -> {
                    val intent = Intent(this, FavoritsActivity::class.java)
                    intent.putExtra("selectedDate", selectedDate)
                    startActivityForResult(intent, REQUEST_CODE_FAVORITES)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun setupWeekResetCheck() {
        val sharedPref = getSharedPreferences("WeeklyPlanPrefs", MODE_PRIVATE)
        val lastResetDate = sharedPref.getString("last_reset_date", "")
        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

        if (lastResetDate.isNullOrEmpty() ||
            sharedPref.getInt("last_reset_week", -1) != currentWeek) {
            mealViewModel.clearOldPlans()
            with(sharedPref.edit()) {
                putString("last_reset_date", selectedDate)
                putInt("last_reset_week", currentWeek)
                apply()
            }
            Toast.makeText(this, "Weekly plan has been reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlannedMealsForDate(date: String) {
        mealViewModel.getPlannedMealsForDate(date).observe(this) { plannedMeals ->
            plannedMealsAdapter.submitList(plannedMeals)
            binding.tvEmpty.visibility = if (plannedMeals.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun hasCalendarPermissions(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCalendarPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            CALENDAR_PERMISSION_REQUEST
        )
    }

    private fun addToDeviceCalendar(plannedMeal: PlannedMeals) {
        if (!hasCalendarPermissions()) {
            requestCalendarPermissions()
            return
        }

        val startTime = parseDateToMillis(plannedMeal.date)
        val endTime = startTime + 3600000

        val event = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, getDefaultCalendarId())
            put(CalendarContract.Events.TITLE, "Meal: ${plannedMeal.mealName}")
            put(CalendarContract.Events.DESCRIPTION,
                "Type: ${plannedMeal.mealType}\nAdded via MealMate")
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        try {
            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, event)
            uri?.let {
                val eventId = ContentUris.parseId(uri).toString()
                mealViewModel.updatePlannedMealWithEventId(plannedMeal.id, eventId)
                Toast.makeText(this, "Added to calendar", Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Failed to add to calendar", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFromDeviceCalendar(plannedMeal: PlannedMeals) {
        plannedMeal.calendarEventId?.let { eventId ->
            try {
                val uri = ContentUris.withAppendedId(
                    CalendarContract.Events.CONTENT_URI,
                    eventId.toLong()
                )
                contentResolver.delete(uri, null, null)
                Toast.makeText(this, "Removed from calendar", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to remove from calendar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDefaultCalendarId(): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0) else 1
        } ?: 1
    }

    private fun parseDateToMillis(dateStr: String): Long {
        return dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Calendar permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Calendar permissions must be granted to use the feature.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val mealId = data.getStringExtra("mealId") ?: return
            val mealName = data.getStringExtra("mealName") ?: "Meal"
            val mealType = data.getStringExtra("mealType") ?: "undefined"
            val date = data.getStringExtra("selectedDate") ?: return
            val mealImage = data.getStringExtra("mealImage") ?: ""

            val newPlannedMeal = PlannedMeals(
                mealId = mealId,
                mealName = mealName,
                mealImage = mealImage,
                date = date,
                mealType = mealType
            )

            mealViewModel.insertPlannedMeal(newPlannedMeal)
            fetchPlannedMealsForDate(selectedDate)
            Toast.makeText(this, "Meal added for the day $date", Toast.LENGTH_SHORT).show()
        }
    }
}