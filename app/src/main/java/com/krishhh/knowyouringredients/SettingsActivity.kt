package com.krishhh.knowyouringredients

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.krishhh.knowyouringredients.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.title = "Settings"
        binding.toolbarSettings.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "system")) {
            "light" -> binding.rbLight.isChecked = true
            "dark"  -> binding.rbDark.isChecked  = true
            else    -> binding.rbSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbLight  -> setThemePref(prefs, "light",  AppCompatDelegate.MODE_NIGHT_NO)
                R.id.rbDark   -> setThemePref(prefs, "dark",   AppCompatDelegate.MODE_NIGHT_YES)
                R.id.rbSystem -> setThemePref(prefs, "system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setThemePref(prefs: android.content.SharedPreferences, key: String, mode: Int) {
        prefs.edit().putString("theme", key).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}
