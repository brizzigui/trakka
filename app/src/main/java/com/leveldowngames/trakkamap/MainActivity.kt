package com.leveldowngames.trakkamap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.leveldowngames.trakkamap.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (!(fineLocationGranted && coarseLocationGranted)) {
                Toast.makeText(this, "Location permissions are needed for the app to work properly.", Toast.LENGTH_SHORT).show()
            }

            if (!hasNotificationPermission()) {
                requestNotificationPermission()
            }

            if (!hasBackgroundLocationPermission())
            {
                showPermissionRationale()
            }
        }


    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (!permission) {
                Toast.makeText(this, "Notification are recommended for the app to work properly.", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            if (!permission) {
                Toast.makeText(this, "Tracking may be inaccurate without background location access.", Toast.LENGTH_SHORT).show()
            }
            else if (!Pinpointer.isRunning)
                startForegroundService(Intent(this, Pinpointer::class.java))
        }


    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Background Location Permission Required")
            .setMessage("Trakka Map requires background location access to accurately track your exploration even when the app is not in use.\n\nOn the following menu, tap 'Allow all the time'.")
            .setPositiveButton("OK") { _, _ ->
                requestBackgroundLocationPermission()
            }
            .create()
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Check and request permissions
        if (!hasLocationPermission()) {
            requestLocationPermission()
        }

        if(hasLocationPermission())
        {
            if (!Pinpointer.isRunning)
                startForegroundService(Intent(this, Pinpointer::class.java))
        }

    }

    // Check if location permissions are granted
    private fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted && coarseLocationGranted
    }

    // Request location permissions
    private fun requestLocationPermission() {
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Check if location permissions are granted
    private fun hasNotificationPermission(): Boolean {
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }

        return notificationGranted
    }

    private fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        else
            true
    }


    // Request notification permissions
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    // Request location permissions
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundLocationPermissionLauncher.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}