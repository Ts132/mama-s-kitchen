package com.example.mealmate2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate2.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            val intent = Intent(this, Hallo::class.java)
            startActivity(intent)
            finish() // لو مش عايزة المستخدم يرجع تاني لصفحة اللوجين بزر الرجوع
        }
        auth = FirebaseAuth.getInstance()
        userDao = MealDatabase.getDatabase(applicationContext).userDao()

        binding.signupbt.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail2.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(name, email, password)
            }
        }


    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // 1. Save login state
                    val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    prefs.edit().putBoolean("isGuest", false).apply()

                    // 2. Save user to Room database
                    val newUser = User(
                        uid = "",  // يمكن تخزين uid إذا كان لديك آلية لتوليده
                        name = name,  // تأكد من تخزين الاسم المدخل
                        email = email
                    )

                    lifecycleScope.launch {
                        userDao.clearAllUsers()
                        userDao.insertUser(newUser)

                        // 3. Go to HomeActivity
                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}