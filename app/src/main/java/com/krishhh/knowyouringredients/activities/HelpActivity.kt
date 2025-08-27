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
        "How do I scan ingredients?" to "Go to the Scan Ingredients section and allow camera permission. Position the product clearly in view and wait for the app to detect the ingredients automatically.",
        "Can I save favorite products?" to "Yes, after scanning a product, click the 'Save' or 'Favorite' button in the scan results to easily access it later from your favorites list.",
        "How do I edit my profile?" to "Open the navigation drawer and click 'Edit Profile' to update your name, profile picture, or dietary preferences.",
        "How to reset my password?" to "Go to Settings and select 'Reset Password'. Follow the prompts to set a new password via your registered email.",
        "Is my data private?" to "Absolutely! All your data is encrypted and stored securely. We do not share personal information with third parties.",
        "How to contact support?" to "Use the 'Feedback' option in the navigation drawer to send your queries or report issues. Our support team will respond promptly.",
        "Can I use the app offline?" to "Most features require internet access, especially ingredient scanning and retrieving product information. Some previously saved favorites may be accessible offline.",
        "How to change diet preferences?" to "Go to Edit Profile and update your dietary preferences, such as vegetarian, vegan, or allergies. This will help the app give more personalized suggestions.",
        "How to delete my account?" to "Contact support via the feedback form. They will guide you through securely deleting your account and all associated data.",
        "How often are nutrition tips updated?" to "Nutrition tips are updated monthly to provide new advice, ingredient suggestions, and healthy lifestyle guidance.",
        "Why does the app sometimes fail to recognize ingredients?" to "Ensure the camera is focused and well-lit, and the ingredient list is clearly visible. Avoid reflections or blurred text for better accuracy.",
        "Can I view past scans?" to "Yes, all your past scans are saved in the History section. Tap any entry to see details, pros/cons, and nutritional information.",
        "How are the pros and cons of ingredients determined?" to "Our app analyzes each ingredient based on trusted nutritional databases and scientific sources to provide accurate pros and cons.",
        "Can I search for a product manually?" to "Yes, use the Search feature to type a product name or ingredient to get its information without scanning.",
        "How do I share scan results?" to "After scanning, tap the 'Share' icon to send the product information via messaging apps or email.",
        "What types of products can I scan?" to "You can scan any packaged food product with a visible ingredient list. The app currently supports most grocery and health food items.",
        "Can I customize notifications for tips or reminders?" to "Go to Settings to enable or disable notifications for new nutrition tips, healthy suggestions, or app updates.",
        "Why is an ingredient marked as 'caution' or 'avoid'?" to "Ingredients are flagged based on health risks, allergens, or dietary restrictions. Check the description for detailed reasoning.",
        "How do I give feedback on the app?" to "Open the Feedback option in the navigation drawer. You can report bugs, suggest new features, or share your experience directly with our team.",
        "Is there a way to export my favorite products or history?" to "Currently, you can view and manage your saved products and history within the app. Export options may be available in future updates."
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
