package com.kennyc.solarviewer.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.card.MaterialCardView
import com.kennyc.solarviewer.R

class StatsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(context, attrs, defStyle) {

    private val title: TextView
    private val energy: TextView
    private val footer: TextView

    init {
        View.inflate(context, R.layout.stat_card, this)
        title = findViewById(R.id.stat_card_title)
        energy = findViewById(R.id.stat_card_energy)
        footer = findViewById(R.id.stat_card_footer)

        if (isInEditMode) setEnergyStat("11.52kW")

        getContext().obtainStyledAttributes(attrs, R.styleable.StatsCard).run {
            getResourceId(R.styleable.StatsCard_stat_icon, RESOURCE_NO_ID)
                .takeIf { it != RESOURCE_NO_ID }
                ?.let {
                    title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, it, 0)
                }

            getString(R.styleable.StatsCard_stat_title)
                .takeIf { !it.isNullOrBlank() }
                ?.let {
                    title.text = it
                }

            getString(R.styleable.StatsCard_stat_footer)
                .takeIf { !it.isNullOrBlank() }
                ?.let {
                    footer.text = it
                }

            recycle()
        }
    }

    fun setEnergyStat(text: String) {
        energy.text = text
    }

    fun setIcon(@DrawableRes icon: Int) {
        title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, icon, 0)
    }

    fun setFooter(text: String) {
        footer.text = text
    }
}

private const val RESOURCE_NO_ID = -1