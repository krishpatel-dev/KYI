package com.krishhh.knowyouringredients

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.krishhh.knowyouringredients.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            val uid = auth.currentUser!!.uid
                            val db = Firebase.firestore
                            val userRef = db.collection("users").document(uid)
                            userRef.get().addOnSuccessListener { snapshot ->
                                if (!snapshot.exists()) {
                                    // Create initial user record
                                    val data = hashMapOf(
                                        "name" to (auth.currentUser?.displayName ?: "User"),
                                        "email" to auth.currentUser?.email!!
                                    )
                                    userRef.set(data)
                                }
                                gotoMain()
                            }
                        } else {
                            Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Google sign-in setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // If user is already logged in, skip login
        auth.currentUser?.let { gotoMain() }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.btnGoogleSignInCustom.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
        }
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email"
            return
        }
        if (pass.length < 6) {
            binding.etPassword.error = "Min 6 chars"
            return
        }

        toggleLoading(true)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                toggleLoading(false)
                if (it.isSuccessful) {
                    if (auth.currentUser!!.isEmailVerified) {
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

    private fun toggleLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
    }

    private fun gotoMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
