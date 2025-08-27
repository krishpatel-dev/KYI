package com.krishhh.knowyouringredients

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.*
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krishhh.knowyouringredients.activities.IngredientDetailActivity
import com.krishhh.knowyouringredients.adapter.SuggestionAdapter
import com.krishhh.knowyouringredients.databinding.ActivityTextSelectionBinding
import com.krishhh.knowyouringredients.db.IngredientDatabase
import com.krishhh.knowyouringredients.utils.HistoryManager
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.math.min

class TextSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextSelectionBinding
    private val words = mutableListOf<Pair<Rect, String>>()
    private val chosen = linkedSetOf<Int>()
    private lateinit var adapter: SuggestionAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        binding = ActivityTextSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBarTheme()
        setupToolbar()
        setupBottomSheet()
        setupRecyclerView()

        val path = intent.getStringExtra(EXTRA_PATH)!!
        val bmp = BitmapFactory.decodeFile(path)

        // Glide crossfade
        Glide.with(this)
            .load(bmp)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.photo)

        binding.photo.post { lifecycleScope.launch { detectWords(bmp) } }

        binding.overlay.setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                val idx = words.indexOfFirst { it.first.contains(e.x.toInt(), e.y.toInt()) }
                if (idx != -1) {
                    if (!chosen.add(idx)) chosen.remove(idx)
                    binding.overlay.selectedBoxes = chosen.map { words[it].first }.toSet()
                    binding.overlay.invalidate() // simple redraw, no animation
                    updateSuggestions()
                }
            }
            true
        }

        savedInstanceState?.getIntegerArrayList("chosen")?.let {
            chosen.clear()
            chosen.addAll(it)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Scan Ingredients"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true
    }

    private fun setupRecyclerView() {
        adapter = SuggestionAdapter { selected ->
            if (selected != "No such product") {
                HistoryManager.saveHistory(this, selected)
                startActivity(IngredientDetailActivity.intent(this, selected))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = adapter
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
        binding.overlay.selectedBoxes = chosen.map { words[it].first }.toSet()
        binding.overlay.invalidate()
        updateSuggestions()
    }

    private fun updateSuggestions() {
        val phrase = chosen.joinToString(" ") { words[it].second }.trim()
        if (phrase.isEmpty()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.tvSelectedText.text = phrase

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

    private fun setupStatusBarTheme() {
        val isLightTheme = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_NO

        window.statusBarColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isLightTheme
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList("chosen", ArrayList(chosen))
        super.onSaveInstanceState(outState)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    companion object {
        private const val EXTRA_PATH = "path"
        fun intent(ctx: Context, path: String): Intent =
            Intent(ctx, TextSelectionActivity::class.java).putExtra(EXTRA_PATH, path)
    }
}
