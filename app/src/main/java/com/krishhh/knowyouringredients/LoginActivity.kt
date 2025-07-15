package com.krishhh.knowyouringredients

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.firebase.auth.*
import com.krishhh.knowyouringredients.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google‑sign‑in launcher
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            rememberUser()      // <- save flag
                            gotoMain()
                        } else {
                            Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Google sign‑in failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth  = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // -------- Google sign‑in client ----------
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // -------- Auto‑login if remembered ----------
        val current = auth.currentUser
        if (current != null && prefs.getBoolean("remember", false)) {
            val verified = current.isEmailVerified ||
                    current.providerData.any { it.providerId == "google.com" }
            if (verified) gotoMain()
        }

        // -------- Click listeners ----------
        binding.btnLogin.setOnClickListener { doLogin() }
        binding.btnGoogleSignInCustom.setOnClickListener { googleLauncher.launch(googleSignInClient.signInIntent) }
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // ========== Email / Password login ==========
    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass  = binding.etPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email"; return
        }
        if (pass.length < 6) {
            binding.etPassword.error = "Min 6 chars"; return
        }

        toggleLoading(true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                toggleLoading(false)
                if (it.isSuccessful) {
                    if (auth.currentUser!!.isEmailVerified) {
                        rememberUser()               // <- save flag
                        gotoMain()
                    } else {
                        Toast.makeText(this, "Verify your email first!", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    // ========== Helpers ==========
    private fun rememberUser() =
        prefs.edit().putBoolean("remember", true).apply()   // always remember on success

    private fun toggleLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
