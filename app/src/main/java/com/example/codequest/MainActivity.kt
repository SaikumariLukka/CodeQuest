package com.example.codequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var btnForgotPassword: Button
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnForgotPassword = findViewById(R.id.btnForgotPassword)
        tvEmailError = findViewById(R.id.tvEmailError)
        tvPasswordError = findViewById(R.id.tvPasswordError)

        // Login button click event
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString()
            val password = etPassword.text.toString()

            // Clear previous error messages
            tvEmailError.visibility = TextView.GONE
            tvPasswordError.visibility = TextView.GONE

            // Call validation methods
            if (validateUsername(email) && validatePassword(password)) {
                // Perform login action here
                // Example: Intent to another activity
            }
        }

        // Register button click event
        btnRegister.setOnClickListener {
            // Navigate to the RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent) // Start the RegisterActivity
        }

        // Forgot Password button click event
        btnForgotPassword.setOnClickListener {
            // Handle forgot password functionality
        }
    }

    // Validate the username (email)
    private fun validateUsername(email: String): Boolean {
        if (email.isEmpty()) {
            tvEmailError.text = "Email cannot be empty"
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        if (email.length < 5 || email.length > 254) {
            tvEmailError.text = "Email must be between 5 and 254 characters."
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.text = "Please enter a valid email address."
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        if (email.contains(" ")) {
            tvEmailError.text = "Email cannot contain spaces."
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        // You need to implement these functions based on your app's logic
        if (isEmailTaken(email)) {
            tvEmailError.text = "This email is already associated with another account. Please choose another."
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        if (isReservedDomain(email)) {
            tvEmailError.text = "Email cannot be from a disposable email service. Please use a valid email address."
            tvEmailError.visibility = TextView.VISIBLE
            return false
        }
        return true
    }

    // Validate the password
    private fun validatePassword(password: String): Boolean {
        if (password.length < 8) {
            tvPasswordError.text = "Password must be at least 8 characters long."
            tvPasswordError.visibility = TextView.VISIBLE
            return false
        }
        // Add more password complexity checks as needed
        return true
    }

    // Dummy methods for email uniqueness and reserved domain checks
    private fun isEmailTaken(email: String): Boolean {
        // Here, implement the logic to check if the email is already taken.
        // This could involve checking a database or an API.
        return false // Change this based on your logic
    }

    private fun isReservedDomain(email: String): Boolean {
        // Implement logic to check if the email domain is reserved or disposable
        return false // Change this based on your logic
    }
}
