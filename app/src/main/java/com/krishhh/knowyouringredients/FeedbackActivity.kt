package com.krishhh.knowyouringredients

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedbackActivity : AppCompatActivity() {

    private lateinit var etFeedback: EditText
    private lateinit var btnSubmit: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Toolbar setup
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarFeedback)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feedback"
        toolbar.setNavigationOnClickListener { finish() }

        etFeedback = findViewById(R.id.etFeedback)
        btnSubmit = findViewById(R.id.btnSubmitFeedback)

        btnSubmit.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = currentUser.uid
            Log.d("FeedbackActivity", "Submitting feedback for UID: $uid")

            val userFeedbackRef = Firebase.firestore
                .collection("feedbacks")
                .document(uid)

            // Store feedback in an array field "feedbackList"
            val data = mapOf("feedbackList" to FieldValue.arrayUnion(feedback))
            userFeedbackRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    etFeedback.text.clear()
                    Log.d("FeedbackActivity", "Feedback successfully submitted")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to submit feedback: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("FeedbackActivity", "Error submitting feedback", e)
                }
        }
    }
}
