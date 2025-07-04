package com.krishhh.knowyouringredients

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.krishhh.knowyouringredients.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        /* Drawer */
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        /* BottomNav */
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
        binding.bottomNav.selectedItemId = R.id.nav_camera

        loadProfile()
    }

    /* ------ Single onNavigationItemSelected for BOTH components ------ */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            /* Drawer items */
            R.id.nav_edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
            R.id.nav_settings     -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_logout       -> {
                FirebaseAuth.getInstance().signOut()
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember", false).apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity(); return true
            }

            /* BottomNav items */
            R.id.nav_search, R.id.nav_camera, R.id.nav_history -> {
                val fragment = when (item.itemId) {
                    R.id.nav_search  -> SearchFragment()
                    R.id.nav_camera  -> CameraFragment()
                    else             -> HistoryFragment()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit()
            }
        }
        // Close drawer if that was the source
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadProfile() {
        val header = binding.navView.getHeaderView(0)
        val iv = header.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivProfile)
        val tv = header.findViewById<android.widget.TextView>(R.id.tvUsername)

        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { snap ->
                if (isDestroyed) return@addOnSuccessListener
                tv.text = snap.getString("name") ?: "Hello!"
                Glide.with(header.context)
                    .load(snap.getString("photoUrl"))
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(iv)
            }
    }

    override fun onResume() {
        super.onResume(); loadProfile()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else finishAffinity()
    }
}
