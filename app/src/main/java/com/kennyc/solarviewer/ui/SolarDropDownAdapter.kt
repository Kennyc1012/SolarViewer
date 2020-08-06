package com.kennyc.solarviewer.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.data.model.SolarSystem

class SolarDropDownAdapter(context: Context, systems: List<SolarSystem>) :
    ArrayAdapter<SolarSystem>(context, R.layout.solar_system_dropdown, systems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent).apply {
            this as TextView
            this.text = getItem(position)?.name
        }
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        getView(position, convertView, parent)
}