package com.kennyc.solarviewer

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import com.kennyc.solarviewer.ui.AppTheme
import java.util.*
import javax.inject.Inject

@ExperimentalComposeUiApi
class MainActivityCompose : ComponentActivity() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val systemsViewModel: SystemsViewModel by viewModels { factory }

    private val formatter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = applicationContext as SolarApp
        app.component.inject(this)

        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen(factory, systemsViewModel)
            }
        }
    }
}