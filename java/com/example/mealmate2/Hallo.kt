package com.example.mealmate2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Hallo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_hallo)
       val btLogin=findViewById<Button>(R.id.loginButton)
        val btSignup=findViewById<Button>(R.id.signupButton)

        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        findViewById<Button>(R.id.btn_guest).setOnClickListener {
            sharedPref.edit().putBoolean("isGuest", true).apply()
            startActivity(Intent(this, HomeActivity::class.java))
        }
        btLogin.setOnClickListener {
            try {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Navigate to SignupActivity
        btSignup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }


    }
}