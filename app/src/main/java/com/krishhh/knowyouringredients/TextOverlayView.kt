package com.krishhh.knowyouringredients

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TextOverlayView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    /** All word boxes */
    var boxes: List<Rect> = emptyList()

    /** Boxes currently selected */
    var selectedBoxes: Set<Rect> = emptySet()

    private val paintBox = Paint().apply {
        color = Color.argb(200, 0, 200, 255)
        style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val paintSel = Paint().apply {
        color = Color.argb(130, 255, 50, 50); style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        boxes.forEach { c.drawRect(it, paintBox) }
        selectedBoxes.forEach { c.drawRect(it, paintSel) }
        if (selectedBoxes.isNotEmpty()) {
            paintSel.alpha = (System.currentTimeMillis()%600/600f*130).toInt()
            postInvalidateOnAnimation()
        }
    }
}
