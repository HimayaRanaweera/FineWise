package com.example.finewise.ui.main

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.finewise.R
import com.example.finewise.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Set up the bottom navigation
        val bottomNav = binding.bottomNav
        bottomNav.setupWithNavController(navController)

        /* Define top level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.fragment_home, R.id.fragment_analysis, R.id.fragment_budget)
        )*/

        // Handle navigation item selection
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_exit -> {
                    showExitDialog()
                    true
                }
                R.id.fragment_home, R.id.fragment_analysis, R.id.fragment_budget -> {
                    try {
                        if (item.itemId != navController.currentDestination?.id) {
                            navController.navigate(item.itemId)
                        }
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                else -> false
            }
        }

        // Ensure the correct item is selected after navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment_home, R.id.fragment_analysis, R.id.fragment_budget -> {
                    bottomNav.menu.findItem(destination.id)?.isChecked = true
                }
            }
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity() // Close all activities
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}