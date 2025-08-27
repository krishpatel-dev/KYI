package com.krishhh.knowyouringredients.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.R
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.krishhh.knowyouringredients.databinding.ActivityIngredientDetailBinding
import com.krishhh.knowyouringredients.db.IngredientDatabase
import com.krishhh.knowyouringredients.db.IngredientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIngredientDetailBinding
    private var hasShownAllergyWarning = false   // 🔑 Flag to avoid repeat dialogs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIngredientDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore flag if activity is recreated
        hasShownAllergyWarning = savedInstanceState?.getBoolean(KEY_WARNING_SHOWN, false) ?: false

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
            if (hit != null) {
                showData(hit)
                checkUserAllergies(hit)
            } else showNoData(phrase)
        }
    }

    private fun showData(e: IngredientEntity) = with(binding) {
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
            val bg = MaterialColors.getColor(this@IngredientDetailActivity, R.attr.colorErrorContainer, 0)
            val fg = MaterialColors.getColor(this@IngredientDetailActivity, R.attr.colorOnErrorContainer, 0)

            for (allergen in allergens) {
                val chip = Chip(this@IngredientDetailActivity).apply {
                    text = allergen
                    isClickable = false
                    isCheckable = false
                    chipBackgroundColor = ColorStateList.valueOf(bg)
                    setTextColor(fg)
                }
                chipGroupAllergens.addView(chip)
            }
        }

        tvNoData.visibility = View.GONE
    }

    private fun checkUserAllergies(e: IngredientEntity) {
        if (hasShownAllergyWarning) return  // 🔑 Prevent showing multiple times

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snap ->
                val allergyString = snap.getString("allergies") ?: return@addOnSuccessListener

                // 🔑 Normalize allergies: ignore "none", "no", "nil", blanks
                val userAllergens = allergyString.split(",")
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() && it !in setOf("none", "no", "nil") }
                    .toSet()

                if (userAllergens.isEmpty()) return@addOnSuccessListener

                // Build a single text to search: combine all ingredient fields
                val productTextBuilder = StringBuilder()
                e.foodProduct?.let { productTextBuilder.append(it).append(" ") }
                e.mainIngredient?.let { productTextBuilder.append(it).append(" ") }
                e.sweetener?.let { productTextBuilder.append(it).append(" ") }
                e.fatOil?.let { productTextBuilder.append(it).append(" ") }
                e.seasoning?.let { productTextBuilder.append(it).append(" ") }
                e.allergens?.let { productTextBuilder.append(it).append(" ") }
                val productText = productTextBuilder.toString().lowercase()

                // Find matches
                val matches = userAllergens.filter { productText.contains(it) }

                if (matches.isNotEmpty()) {
                    hasShownAllergyWarning = true // 🔑 Set flag
                    AlertDialog.Builder(this)
                        .setTitle("Allergy Warning!")
                        .setMessage("This product contains: ${matches.joinToString(", ")}. Be careful!")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
    }

    private fun showNoData(q: String) = with(binding) {
        tvNoData.text = "No information found for \"$q\""
        tvNoData.visibility = View.VISIBLE
        chipGroupAllergens.removeAllViews()
        tvFoodProduct.text = q
        tvMainIngredient.text = "—"
        tvSweetener.text = "—"
        tvFatOil.text = "—"
        tvSeasoning.text = "—"
        tvPrediction.text = "—"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_WARNING_SHOWN, hasShownAllergyWarning)
    }

    companion object {
        private const val EXTRA_PHRASE = "phrase"
        private const val KEY_WARNING_SHOWN = "hasShownAllergyWarning"

        fun intent(ctx: Context, phrase: String) =
            Intent(ctx, IngredientDetailActivity::class.java)
                .putExtra(EXTRA_PHRASE, phrase)
    }
}
