package com.krishhh.knowyouringredients

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.krishhh.knowyouringredients.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupPasswordToggleBehavior()

        // Email-password signup
        binding.btnSignup.setOnClickListener { doSignup() }

        // Google sign up
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false) // always show account picker
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        binding.btnGoogleSignup.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        REQ_ONE_TAP,
                        null, 0, 0, 0
                    )
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun doSignup() {
        val email = binding.etSignupEmail.text.toString().trim()
        val pass = binding.etSignupPassword.text.toString().trim()
        val confirm = binding.etSignupConfirmPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupEmail.error = "Invalid Email"
            return
        }

        if (pass.length < 6) {
            binding.etSignupPassword.error = "Minimum 6 characters"
            return
        }

        if (pass != confirm) {
            binding.etSignupConfirmPassword.error = "Passwords do not match"
            return
        }

        binding.btnSignup.isEnabled = false

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                binding.btnSignup.isEnabled = true
                if (it.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Check email for verification", Toast.LENGTH_LONG).show()
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                        }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP && resultCode == Activity.RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun setupPasswordToggleBehavior() {
        val toggleFields = listOf(
            binding.etSignupPassword to binding.etSignupPasswordLayout,
            binding.etSignupConfirmPassword to binding.etSignupConfirmPasswordLayout
        )

        toggleFields.forEach { (editText, layout) ->
            layout.endIconMode = com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
            layout.isEndIconVisible = false

            editText.addTextChangedListener {
                layout.isEndIconVisible = !it.isNullOrEmpty()
            }
        }
    }

}
