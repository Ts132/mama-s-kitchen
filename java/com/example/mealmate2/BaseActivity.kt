package com.example.mealmate2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
abstract class BaseActivity : AppCompatActivity() {

    abstract val bottomNavigationView: BottomNavigationView

    // ➕ دالة بتحدد أيقونة الصفحة الحالية
    open fun getCurrentNavItemId(): Int? = null

    override fun onStart() {
        super.onStart()

        val isGuest = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getBoolean("isGuest", false)

        if (isGuest) {
            bottomNavigationView.menu.findItem(R.id.nav_favorites)?.isVisible = false
            bottomNavigationView.menu.findItem(R.id.nav_plan)?.isVisible = false
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favorites -> {
                    if (!isGuest) {
                        startActivity(Intent(this, FavoritsActivity::class.java))
                    } else {
                        Toast.makeText(this, "Login required to view favorites", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                }
                R.id.nav_plan -> {
                    if (!isGuest) {
                        startActivity(Intent(this, CalendarActivity::class.java))
                    } else {
                        Toast.makeText(this, "Login required to plan meals", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.action_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            }
            true
        }

        val currentItemId = getCurrentNavItemId()
        if (currentItemId != null) {
            bottomNavigationView.menu.findItem(currentItemId)?.isChecked = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val isGuest = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getBoolean("isGuest", false)

        if (isGuest) {
            menu?.findItem(R.id.action_logout)?.isVisible = false
        }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_home -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> logoutUser() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        CoroutineScope(Dispatchers.IO).launch {
            val db = MealDatabase.getDatabase(applicationContext)
            db.userDao().clearAllUsers()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@BaseActivity, "تم تسجيل الخروج", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@BaseActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun hideIfGuest(viewId: Int) {
        val isGuest = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getBoolean("isGuest", false)

        if (isGuest) {
            findViewById<android.view.View>(viewId)?.visibility = android.view.View.GONE
        }
    }
}
