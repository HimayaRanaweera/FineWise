package com.example.finewise

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.finewise.databinding.ActivitySplashBinding
import com.example.finewise.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply animations
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        val textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
        
        // Start logo animation
        binding.logo.startAnimation(logoAnimation)
        
        // Start text animation with a slight delay
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvAppName.startAnimation(textAnimation)
            binding.tvTagline.startAnimation(textAnimation)
        }, 300)

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
} 