package com.kennyc.solarviewer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kennyc.solarviewer.R

class DonutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val outer: ProgressBar
    private val inner: ProgressBar
    private val label: TextView

    init {
        View.inflate(context, R.layout.donut_layout, this)
        outer = findViewById(R.id.donut_total)
        inner = findViewById(R.id.donut_slice)
        label = findViewById(R.id.donut_text)
    }

    fun setDonutStats(total: Int, slice: Int, text: String) {
        outer.max = total
        outer.progress = total
        inner.max = total
        inner.progress = slice
        label.text = text
    }
}