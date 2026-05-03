package com.kennyc.solarviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.kennyc.solarviewer.auth.AuthState
import com.kennyc.solarviewer.auth.EnphaseAuthManager
import com.kennyc.solarviewer.auth.LoginScreen
import com.kennyc.solarviewer.ui.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalComposeUiApi
@AndroidEntryPoint
class MainActivityCompose : ComponentActivity() {

    @Inject
    lateinit var authManager: EnphaseAuthManager

    private val systemsViewModel: SystemsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch { authManager.refreshAuthState() }

        handleAuthIntent(intent)

        setContent {
            AppTheme {
                val authState by authManager.state.collectAsStateWithLifecycle()

                when (val s = authState) {
                    AuthState.Authorized -> MainScreen(systemsViewModel)
                    is AuthState.Error -> LoginScreen(errorMessage = s.message) {
                        lifecycleScope.launch { authManager.startAuthorize(this@MainActivityCompose) }
                    }
                    AuthState.Unauthorized,
                    AuthState.Unknown -> LoginScreen {
                        lifecycleScope.launch { authManager.startAuthorize(this@MainActivityCompose) }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (!authManager.isCallback(data)) return
        lifecycleScope.launch { authManager.handleCallback(data) }
    }
}
