package com.krishhh.knowyouringredients

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.krishhh.knowyouringredients.databinding.ActivityIngredientDetailBinding
import com.krishhh.knowyouringredients.db.IngredientDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIngredientDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngredientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        binding.toolbarDetail.setNavigationOnClickListener { finish() }

        val rawPhrase = intent.getStringExtra(EXTRA_PHRASE).orEmpty()
        val phrase = rawPhrase.trim().replace(Regex("\\s+"), " ")
        title = phrase

        lifecycleScope.launch {
            val dao = IngredientDatabase.get(this@IngredientDetailActivity).dao()
            val hit = withContext(Dispatchers.IO) {
                dao.findExact(phrase) ?: dao.findLike(phrase)
                ?: phrase.split(' ').firstNotNullOfOrNull { dao.findContaining(it) }
            }
            if (hit != null) showData(hit) else showNoData(phrase)
        }
    }

    private fun showData(e: com.krishhh.knowyouringredients.db.IngredientEntity) = with(binding) {
        tvFoodProduct.text = e.foodProduct
        tvMainIngredient.text = e.mainIngredient ?: "—"
        tvSweetener.text = e.sweetener ?: "—"
        tvFatOil.text = e.fatOil ?: "—"
        tvSeasoning.text = e.seasoning ?: "—"
        tvPrediction.text = e.prediction ?: "—"

        // Allergens ChipGroup
        chipGroupAllergens.removeAllViews()
        val allergens = e.allergens?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        if (allergens.isNotEmpty()) {
            val bg = MaterialColors.getColor(this@IngredientDetailActivity, com.google.android.material.R.attr.colorErrorContainer, 0)
            val fg = MaterialColors.getColor(this@IngredientDetailActivity, com.google.android.material.R.attr.colorOnErrorContainer, 0)

            for (allergen in allergens) {
                val chip = Chip(this@IngredientDetailActivity).apply {
                    text = allergen
                    isClickable = false
                    isCheckable = false
                    chipBackgroundColor = android.content.res.ColorStateList.valueOf(bg)
                    setTextColor(fg)
                }
                chipGroupAllergens.addView(chip)
            }
        }

        tvNoData.visibility = View.GONE
    }

    private fun showNoData(q: String) = with(binding) {
        tvNoData.text = "No information found for \"$q\""
        tvNoData.visibility = View.VISIBLE
        // Hide chips if any
        chipGroupAllergens.removeAllViews()
        tvFoodProduct.text = q
        tvMainIngredient.text = "—"
        tvSweetener.text = "—"
        tvFatOil.text = "—"
        tvSeasoning.text = "—"
        tvPrediction.text = "—"
    }

    companion object {
        private const val EXTRA_PHRASE = "phrase"
        fun intent(ctx: Context, phrase: String) =
            Intent(ctx, IngredientDetailActivity::class.java)
                .putExtra(EXTRA_PHRASE, phrase)
    }
}
