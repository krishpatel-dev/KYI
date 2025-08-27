package com.krishhh.knowyouringredients.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.krishhh.knowyouringredients.adapter.NutritionTipsAdapter
import com.krishhh.knowyouringredients.databinding.ActivityNutritionTipsBinding

class NutritionTipsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNutritionTipsBinding

    private val tipsList = listOf(
        "Drink plenty of water" to "Water helps maintain bodily functions and improves skin health.",
        "Eat more vegetables" to "Vegetables are rich in fiber, vitamins, and minerals.",
        "Include protein" to "Protein is essential for muscle growth and repair.",
        "Limit sugar" to "Too much sugar can lead to weight gain and energy crashes.",
        "Eat whole grains" to "Whole grains support digestion and provide long-lasting energy.",
        "Healthy fats" to "Include nuts, seeds, and avocado for heart health.",
        "Control portion size" to "Helps prevent overeating and maintains weight.",
        "Regular meals" to "Avoid skipping meals for better metabolism.",
        "Include fruits" to "Fruits provide antioxidants and essential vitamins.",
        "Reduce processed food" to "Minimize processed foods for better overall health."
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
