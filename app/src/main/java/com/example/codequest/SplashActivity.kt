package com.example.codequest // Replace with your actual package name

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // Duration of the splash screen in milliseconds (3 seconds)
    private val splashScreenDuration: Long = 3500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Get the TextView and load the animation
        val appNameTextView = findViewById<TextView>(R.id.tvAppName)
        val fadeInAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Apply the animation to the TextView
        appNameTextView.startAnimation(fadeInAnimation)

        // Handler to delay the transition to the login activity
        Handler().postDelayed({
            // Start the LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish the SplashActivity so it doesn't remain in the back stack
        }, splashScreenDuration)
    }
}
