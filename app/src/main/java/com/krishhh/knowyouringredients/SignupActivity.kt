package com.krishhh.knowyouringredients

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.krishhh.knowyouringredients.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.btnSignup.setOnClickListener { doSignup() }
    }

    private fun doSignup() {
        val email   = binding.etSignupEmail.text.toString().trim()
        val pass    = binding.etSignupPassword.text.toString().trim()
        val confirm = binding.etSignupConfirmPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupEmail.error = "Invalid"; return
        }
        if (pass.length < 6) {
            binding.etSignupPassword.error = "Min 6"; return
        }
        if (pass != confirm) {
            binding.etSignupConfirmPassword.error = "No match"; return
        }

        binding.btnSignup.isEnabled = false
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Check your email to verify account", Toast.LENGTH_LONG).show()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "Verification email failed", Toast.LENGTH_SHORT).show()
                        }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
