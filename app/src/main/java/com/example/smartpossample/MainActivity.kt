package com.example.smartpossample

import android.os.Bundle
import android.view.WindowInsets
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.smartpossample.databinding.ActivityMainBinding
import eu.nets.lab.smartpos.sdk.Log
import eu.nets.lab.smartpos.sdk.client.NetsClient
import eu.nets.lab.smartpos.sdk.immersiveMode
import eu.nets.lab.smartpos.sdk.payload.AdminRequest
import eu.nets.lab.smartpos.sdk.payload.currencyErrorOrNull
import eu.nets.lab.smartpos.sdk.payload.currencyResultOrNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        ViewCompat.setOnApplyWindowInsetsListener(navView) { view, insets ->
            insets
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        NetsClient.get().use { client ->
            val requestPayload = AdminRequest.currencyRequest
            client.advancedAdminManager(this).register { result ->
                result.currencyResultOrNull()?.let {
                    sharedPreferences.edit().putString("preference_currency", it.symbol).apply()
                } ?: result.currencyErrorOrNull()?.let {
                    Log.e("MainActivity") { "Error getting currency: ${it.second}" }
                }
            }.process(requestPayload)
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_sales, R.id.navigation_refunds, R.id.navigation_others
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        this.immersiveMode()
    }
}