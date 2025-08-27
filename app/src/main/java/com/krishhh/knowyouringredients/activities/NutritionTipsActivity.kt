package com.krishhh.knowyouringredients.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.krishhh.knowyouringredients.adapter.NutritionTipsAdapter
import com.krishhh.knowyouringredients.databinding.ActivityNutritionTipsBinding

class NutritionTipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNutritionTipsBinding

    private val tipsList = listOf(
        "Drink plenty of water" to "Water helps maintain bodily functions, supports digestion, regulates body temperature, flushes out toxins, improves skin health, boosts energy levels, and aids in nutrient absorption.",
        "Eat more vegetables" to "Vegetables are packed with fiber, vitamins, and minerals which support overall health. They help in digestion, reduce inflammation, strengthen immunity, and can prevent chronic diseases like diabetes and heart disease.",
        "Include protein" to "Protein is essential for muscle growth, repair, and maintenance. It also supports immune function, hormone production, and can keep you fuller for longer, reducing unnecessary snacking.",
        "Limit sugar" to "Excess sugar can cause weight gain, energy spikes and crashes, and increase the risk of diabetes, heart disease, and tooth decay. Opt for natural sugars from fruits instead of processed sugars.",
        "Eat whole grains" to "Whole grains like oats, brown rice, and quinoa support digestion, provide long-lasting energy, and help regulate blood sugar levels. They are rich in fiber, B vitamins, and minerals like iron and magnesium.",
        "Healthy fats" to "Healthy fats from sources like nuts, seeds, olive oil, and avocado support heart health, brain function, hormone production, and absorption of fat-soluble vitamins (A, D, E, K).",
        "Control portion size" to "Eating the right portions helps prevent overeating and maintain a healthy weight. It also helps manage blood sugar levels and ensures balanced nutrient intake.",
        "Regular meals" to "Having regular meals prevents extreme hunger, stabilizes metabolism, maintains energy levels, and supports better digestion. Skipping meals can lead to overeating later and decreased focus.",
        "Include fruits" to "Fruits are rich in antioxidants, vitamins, minerals, and fiber. They support immunity, reduce inflammation, promote healthy skin, and can satisfy sweet cravings in a nutritious way.",
        "Reduce processed food" to "Processed foods often contain high levels of sugar, salt, unhealthy fats, and preservatives. Minimizing them improves heart health, aids weight management, and reduces the risk of chronic diseases.",
        "Get enough sleep" to "Quality sleep of 7–9 hours is vital for physical and mental health. It supports memory, mood, immune function, and helps the body repair and rejuvenate.",
        "Exercise regularly" to "Regular physical activity strengthens the heart, muscles, and bones, improves mood, boosts metabolism, and helps maintain a healthy weight. Aim for at least 150 minutes of moderate activity weekly.",
        "Manage stress" to "Chronic stress affects immunity, digestion, and mental health. Techniques like meditation, deep breathing, and hobbies help reduce stress and improve overall wellbeing.",
        "Limit alcohol intake" to "Excessive alcohol can damage the liver, heart, and brain, and contribute to weight gain. Drink in moderation and prioritize water and healthier beverages.",
        "Avoid smoking" to "Smoking increases the risk of cancer, heart disease, respiratory problems, and premature aging. Quitting smoking greatly improves health and life expectancy.",
        "Eat mindfully" to "Pay attention to your meals, chew slowly, and avoid distractions. Mindful eating improves digestion, prevents overeating, and helps you enjoy food more.",
        "Include probiotics" to "Probiotics like yogurt, kefir, and fermented foods support gut health, boost immunity, and aid digestion.",
        "Limit salt intake" to "Excess salt can increase blood pressure and risk of heart disease. Use herbs, spices, and natural flavors instead of extra salt.",
        "Stay socially connected" to "Strong social connections improve mental health, reduce stress, and can even boost longevity. Spend quality time with friends and family.",
        "Practice good hygiene" to "Regular handwashing, oral care, and personal hygiene reduce infections, improve overall health, and promote wellbeing."
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNutritionTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ─── Toolbar setup ───
        setSupportActionBar(binding.toolbarNutrition)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarNutrition.setNavigationOnClickListener { finish() }
        supportActionBar?.title = "Nutrition Tips"

        // ─── RecyclerView setup ───
        binding.rvNutritionTips.layoutManager = LinearLayoutManager(this)
        binding.rvNutritionTips.adapter = NutritionTipsAdapter(tipsList)
    }
}
