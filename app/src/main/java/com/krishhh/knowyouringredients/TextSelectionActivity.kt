package com.krishhh.knowyouringredients

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krishhh.knowyouringredients.databinding.ActivityTextSelectionBinding
import com.krishhh.knowyouringredients.db.IngredientDatabase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.krishhh.knowyouringredients.utils.HistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.min

class TextSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextSelectionBinding
    private val words = mutableListOf<Pair<Rect, String>>()
    private val chosen = linkedSetOf<Int>()
    private lateinit var adapter: SuggestionAdapter

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomSheetLayout = findViewById<LinearLayout>(R.id.bottomSheetContainer)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Scan Ingredients"  // Left-aligned by default
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val path = intent.getStringExtra(EXTRA_PATH)!!
        val bmp = BitmapFactory.decodeFile(path)
        binding.photo.setImageBitmap(bmp)

        adapter = SuggestionAdapter { selected ->
            if (selected != "No such product") {
                HistoryManager.saveHistory(this, selected)
                startActivity(IngredientDetailActivity.intent(this, selected))
            }
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapter

        binding.photo.post { lifecycleScope.launch { detectWords(bmp) } }

        binding.overlay.setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                val idx = words.indexOfFirst { it.first.contains(e.x.toInt(), e.y.toInt()) }
                if (idx != -1) {
                    if (!chosen.add(idx)) chosen.remove(idx)
                    binding.overlay.selectedBoxes = chosen.map { words[it].first }.toSet()
                    binding.overlay.invalidate()
                    updateSuggestions()
                }
            }
            true
        }
    }

    private suspend fun detectWords(bmp: Bitmap) {
        val rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = rec.process(InputImage.fromBitmap(bmp, 0)).await()

        val vw = binding.photo.width.toFloat()
        val vh = binding.photo.height.toFloat()
        val iw = bmp.width.toFloat()
        val ih = bmp.height.toFloat()

        val scale = min(vw / iw, vh / ih)
        val offX = (vw - iw * scale) / 2
        val offY = (vh - ih * scale) / 2

        fun map(r: Rect) = Rect(
            (r.left * scale + offX).toInt(),
            (r.top * scale + offY).toInt(),
            (r.right * scale + offX).toInt(),
            (r.bottom * scale + offY).toInt()
        )

        result.textBlocks.forEach { b ->
            b.lines.forEach { l ->
                l.elements.forEach { e ->
                    e.boundingBox?.let { words += map(it) to e.text }
                }
            }
        }
        binding.overlay.boxes = words.map { it.first }
        binding.overlay.invalidate()
    }

    private fun updateSuggestions() {
        val phrase = chosen.map { words[it].second }.joinToString(" ").trim()
        if (phrase.isEmpty()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.tvSelectedText.text = phrase  // ✅ Show selected text beside label

        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                val dao = IngredientDatabase.get(this@TextSelectionActivity).dao()
                val keywords = phrase.split(" ").filter { it.isNotBlank() }

                val allMatches = mutableSetOf<String>()
                for (word in keywords) {
                    allMatches += dao.searchSuggestions(word)
                }

                if (allMatches.isEmpty()) listOf("No such product") else allMatches.toList()
            }
            adapter.submitList(results)
        }
    }

    companion object {
        private const val EXTRA_PATH = "path"
        fun intent(ctx: Context, path: String) =
            Intent(ctx, TextSelectionActivity::class.java).putExtra(EXTRA_PATH, path)
    }
}
