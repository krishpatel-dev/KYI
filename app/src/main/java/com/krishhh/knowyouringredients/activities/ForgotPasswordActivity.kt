package com.krishhh.knowyouringredients.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.krishhh.knowyouringredients.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.btnResetPassword.setOnClickListener { sendReset() }
    }
    private fun sendReset(){
        val email=binding.etForgotEmail.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){ binding.etForgotEmail.error="Invalid"; return }
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener{
                if(it.isSuccessful){ Toast.makeText(this,"Reset link sent!",Toast.LENGTH_LONG).show(); finish() }
                else Toast.makeText(this,it.exception?.localizedMessage,Toast.LENGTH_LONG).show()
            }
    }
}
