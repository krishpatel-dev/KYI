package com.krishhh.knowyouringredients.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.krishhh.knowyouringredients.R
import com.krishhh.knowyouringredients.databinding.ActivitySettingsBinding
import com.krishhh.knowyouringredients.utils.HistoryManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.title = "Settings"
        binding.toolbarSettings.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // --- Theme ---
        when (prefs.getString("theme", "system")) {
            "light" -> binding.rbLight.isChecked = true
            "dark" -> binding.rbDark.isChecked = true
            else -> binding.rbSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbLight -> setThemePref(prefs, "light", AppCompatDelegate.MODE_NIGHT_NO)
                R.id.rbDark -> setThemePref(prefs, "dark", AppCompatDelegate.MODE_NIGHT_YES)
                R.id.rbSystem -> setThemePref(prefs, "system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // --- Notifications ---
        val switchPush: Switch = binding.switchPush
        val switchPromo: Switch = binding.switchPromo
        switchPush.isChecked = prefs.getBoolean("push_notifications", true)
        switchPromo.isChecked = prefs.getBoolean("promo_notifications", true)

        switchPush.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("push_notifications", isChecked).apply()
        }
        switchPromo.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("promo_notifications", isChecked).apply()
        }

        // --- Data Sharing ---
        val switchData: Switch = binding.switchDataSharing
        switchData.isChecked = prefs.getBoolean("data_sharing", true)
        switchData.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("data_sharing", isChecked).apply()
        }

        // --- Language ---
        val languages = listOf("English", "Hindi", "Spanish")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = binding.spinnerLanguage
        spinner.adapter = adapter
        spinner.setSelection(languages.indexOf(prefs.getString("language", "English")))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                prefs.edit().putString("language", languages[position]).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: do nothing
            }
        }


        // --- Clear History ---
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all history?")
                .setPositiveButton("Yes") { dialog, _ ->
                    HistoryManager.clearHistory(this)
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()

                    // Notify previous activity/fragment
                    setResult(RESULT_OK)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }


        // --- Delete Account ---
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    deleteUserAccount()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // --- Reset Preferences ---
        binding.btnResetPrefs.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Preferences")
                .setMessage("Are you sure you want to reset all preferences to default?")
                .setPositiveButton("Reset") { dialog, _ ->
                    prefs.edit().clear().apply()
                    recreate() // reload activity with defaults
                    Toast.makeText(this, "Preferences reset", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun setThemePref(prefs: SharedPreferences, key: String, mode: Int) {
        prefs.edit().putString("theme", key).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun deleteUserAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return

        // Delete Firestore data first
        val db = FirebaseFirestore.getInstance()
        db.collection("feedbacks").document(uid).delete()
        db.collection("users").document(uid).delete()
            .addOnSuccessListener {
                // Delete Firebase Auth account
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

                        // Sign out just in case
                        FirebaseAuth.getInstance().signOut()

                        // Redirect to LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete account: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

}
