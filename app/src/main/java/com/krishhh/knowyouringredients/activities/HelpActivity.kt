package com.krishhh.knowyouringredients.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.krishhh.knowyouringredients.R

class HelpActivity : AppCompatActivity() {

    private val faqs = listOf(
        "How do I scan ingredients?" to "Go to Scan Ingredients section and allow camera permission.",
        "Can I save favorite products?" to "Yes, you can save them from the scan results.",
        "How do I edit my profile?" to "Click Edit Profile from the navigation drawer.",
        "How to reset my password?" to "Go to Settings and select Reset Password.",
        "Is my data private?" to "Yes, we follow strict privacy policies.",
        "How to contact support?" to "Use the Feedback option in navigation drawer.",
        "Can I use the app offline?" to "Most features require internet access.",
        "How to change diet preferences?" to "Edit your profile and update diet type.",
        "How to delete my account?" to "Contact support via feedback form.",
        "How often are nutrition tips updated?" to "Tips are updated monthly."
    )

    // Track expanded/collapsed state of FAQs
    private val faqsExpandedState = mutableListOf<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarHelp)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Help"
        toolbar.setNavigationOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.llFaqContainer)
        val inflater = LayoutInflater.from(this)

        // Initialize all FAQ states
        faqs.forEach { _ -> faqsExpandedState.add(false) }

        faqs.forEachIndexed { index, (question, answer) ->
            val cardView = inflater.inflate(R.layout.item_faq_card, container, false)

            val tvQuestion = cardView.findViewById<TextView>(R.id.tvQuestion)
            val tvAnswer = cardView.findViewById<TextView>(R.id.tvAnswer)

            tvQuestion.text = question
            tvAnswer.text = answer
            tvAnswer.visibility = if (faqsExpandedState[index]) View.VISIBLE else View.GONE

            cardView.setOnClickListener {
                // Toggle visibility and update state
                val isExpanded = tvAnswer.visibility == View.VISIBLE
                tvAnswer.visibility = if (isExpanded) View.GONE else View.VISIBLE
                faqsExpandedState[index] = !isExpanded
            }

            container.addView(cardView)
        }

        // Restore expanded state after configuration change (theme change)
        savedInstanceState?.getBooleanArray("faqStates")?.let { savedStates ->
            savedStates.forEachIndexed { index, expanded ->
                faqsExpandedState[index] = expanded
                val cardView = container.getChildAt(index) // index matches faqs list
                val tvAnswer = cardView.findViewById<TextView>(R.id.tvAnswer)
                tvAnswer.visibility = if (expanded) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray("faqStates", faqsExpandedState.toBooleanArray())
    }
}
