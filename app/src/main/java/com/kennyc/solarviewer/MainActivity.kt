package com.kennyc.solarviewer

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.kennyc.solarviewer.data.model.SolarSystem
import com.kennyc.solarviewer.ui.SolarDropDownAdapter
import java.time.ZoneId
import java.util.*
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private lateinit var viewModel: SystemsViewModel

    private val formatter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

    private lateinit var date: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext as SolarApp
        app.component.inject(this)

        setContentView(R.layout.activity_main)
        date = findViewById(R.id.systems_date)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        NavigationUI.setupWithNavController(bottomNavigationView, navHostFragment.navController)

        viewModel = ViewModelProvider(this, factory).get(SystemsViewModel::class.java)
        viewModel.systems.observe(this, Observer {
            findViewById<Spinner>(R.id.systems_spinner).apply {
                adapter = SolarDropDownAdapter(this@MainActivity, it)

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // NOOP
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.onSystemSelected(adapter.getItem(position) as SolarSystem)
                    }
                }
                isEnabled = it.size > 1
            }
        })

        viewModel.date.observe(this, Observer {
            date.text = formatter.format(it)
        })

        findViewById<Button>(R.id.systems_date).setOnClickListener {
            MaterialDatePicker.Builder
                .datePicker()
                .build()
                .apply {
                    addOnPositiveButtonClickListener { time ->
                        val offset = TimeZone.getTimeZone(ZoneId.systemDefault()).getOffset(time)
                        viewModel.setNewDate(Date(time - offset))
                    }
                }.show(supportFragmentManager, null)
        }
    }
}