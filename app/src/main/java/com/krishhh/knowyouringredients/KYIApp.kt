package com.krishhh.knowyouringredients

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class KYIApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        when (prefs.getString("app_theme", "SYSTEM")) {
            "LIGHT"  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "DARK"   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else     -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
