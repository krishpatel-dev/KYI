package com.krishhh.knowyouringredients

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krishhh.knowyouringredients.databinding.ActivityTextSelectionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.min

class TextSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextSelectionBinding

    /** All detected words in reading order */
    private val words = mutableListOf<Pair<Rect, String>>()

    /** Indices of words currently selected */
    private val chosen = linkedSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val path = intent.getStringExtra(EXTRA_PATH)!!
        val bmp  = BitmapFactory.decodeFile(path)
        binding.photo.setImageBitmap(bmp)

        /* Run OCR after ImageView is laid out */
        binding.photo.post { lifecycleScope.launch { detectWords(bmp) } }

        /* ───── TAP HANDLER ───── */
        binding.overlay.setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_DOWN) {
                val idx = words.indexOfFirst { it.first.contains(e.x.toInt(), e.y.toInt()) }
                if (idx != -1) {
                    if (!chosen.add(idx)) chosen.remove(idx) // toggle
                    binding.overlay.selectedBoxes =
                        chosen.map { words[it].first }.toSet()
                    binding.overlay.invalidate()
                    updateFabLabel()
                }
            }
            true
        }

        /* ───── Go button ───── */
        binding.fabGo.setOnClickListener {
            val phrase = currentPhrase()
            if (phrase.isEmpty()) toast("Tap words to build a phrase")
            else startActivity(IngredientDetailActivity.intent(this, phrase))
        }
    }

    /* ───────── OCR & mapping ───────── */
    private suspend fun detectWords(bmp: Bitmap) {
        val rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = rec.process(InputImage.fromBitmap(bmp, 0)).await()

        val vw = binding.photo.width.toFloat()
        val vh = binding.photo.height.toFloat()
        val iw = bmp.width.toFloat()
        val ih = bmp.height.toFloat()

        val scale   = min(vw/iw, vh/ih)
        val offX    = (vw - iw*scale)/2
        val offY    = (vh - ih*scale)/2

        fun map(r: Rect) = Rect(
            (r.left*scale + offX).toInt(),
            (r.top *scale + offY).toInt(),
            (r.right*scale + offX).toInt(),
            (r.bottom*scale+ offY).toInt()
        )

        // Collect words top‑to‑bottom, left‑to‑right
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

    /* ───────── helpers ───────── */
    private fun currentPhrase(): String =
        chosen.map { words[it].second }
            .joinToString(" ")

    private fun updateFabLabel() {
        binding.fabGo.contentDescription = currentPhrase()
    }

    private fun toast(msg: String) =
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()

    companion object {
        private const val EXTRA_PATH = "path"
        fun intent(ctx: Context, path: String) =
            Intent(ctx, TextSelectionActivity::class.java).putExtra(EXTRA_PATH, path)
    }
}
