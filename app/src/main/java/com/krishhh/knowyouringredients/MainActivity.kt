package com.krishhh.knowyouringredients

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.krishhh.knowyouringredients.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        loadProfileIntoDrawer()
    }

    private fun loadProfileIntoDrawer() {
        val header = binding.navView.getHeaderView(0)
        val iv = header.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfile)
        val tv = header.findViewById<android.widget.TextView>(R.id.tvUsername)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snap ->
                tv.text = snap.getString("name") ?: "Hello!"
                Glide.with(this)
                    .load(snap.getString("photoUrl"))
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(iv)
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
            R.id.nav_settings     -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_logout       -> {
                FirebaseAuth.getInstance().signOut()
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember", false).apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        loadProfileIntoDrawer()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else finishAffinity()
    }
}
