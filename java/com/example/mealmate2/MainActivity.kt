package com.example.mealmate2

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val splashTime: Long = 5000 // مدة عرض Splash Screen (3 ثواني)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // إخفاء الـ Action Bar
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            val sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPrefs.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                if (isInternetAvailable()) {
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this@MainActivity, FavoritsActivity::class.java))
                }
            } else {
                startActivity(Intent(this@MainActivity, Hallo::class.java))
            }
            finish()
        }, splashTime)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
