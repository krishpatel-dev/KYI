package com.krishhh.knowyouringredients.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.krishhh.knowyouringredients.fragments.CameraFragment
import com.krishhh.knowyouringredients.fragments.HistoryFragment
import com.krishhh.knowyouringredients.MainViewModel
import com.krishhh.knowyouringredients.R
import com.krishhh.knowyouringredients.fragments.SearchFragment
import com.krishhh.knowyouringredients.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        var instance: MainActivity? = null
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var viewModel: MainViewModel
    private var currentSelectedItemId: Int = R.id.nav_camera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Know Your Ingredients"

        // Drawer setup
        toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        binding.bottomNav.setOnNavigationItemSelectedListener(this)

        // Ensure user is logged in
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Load Firestore profile
        viewModel.loadUserProfile { name, photo ->
            viewModel.localUserName.value = name
            viewModel.photoUrl = photo
            loadProfile()
        }

        // Observe LiveData
        lifecycleScope.launch { viewModel.localPhotoUri.collect { loadProfile() } }
        lifecycleScope.launch { viewModel.localUserName.collect { loadProfile() } }

        // Load first fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CameraFragment())
                .commit()
            binding.bottomNav.selectedItemId = R.id.nav_camera
        } else {
            currentSelectedItemId = savedInstanceState.getInt("current_tab", R.id.nav_camera)
            binding.bottomNav.selectedItemId = currentSelectedItemId
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Drawer actions
            R.id.nav_edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_tips -> startActivity(Intent(this, NutritionTipsActivity::class.java))
            R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            R.id.nav_feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
            R.id.nav_logout -> showLogoutConfirmation()

            // BottomNav actions
            R.id.nav_search, R.id.nav_camera, R.id.nav_history -> {
                if (item.itemId != currentSelectedItemId) {
                    val fragment = when (item.itemId) {
                        R.id.nav_search -> SearchFragment()
                        R.id.nav_camera -> CameraFragment()
                        else -> HistoryFragment()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit()
                    currentSelectedItemId = item.itemId
                }
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun loadProfile() {
        val header = binding.navView.getHeaderView(0)
        val iv = header.findViewById<ShapeableImageView>(R.id.ivProfile)
        val tv = header.findViewById<TextView>(R.id.tvUsername)

        tv.text = viewModel.localUserName.value ?: "Hello!"
        Glide.with(header.context)
            .load(viewModel.localPhotoUri.value ?: viewModel.photoUrl)
            .placeholder(R.mipmap.ic_launcher_round)
            .into(iv)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finishAffinity()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_tab", currentSelectedItemId)
    }
}
