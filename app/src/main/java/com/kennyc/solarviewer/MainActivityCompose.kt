package com.kennyc.solarviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import com.kennyc.solarviewer.auth.AuthState
import com.kennyc.solarviewer.auth.EnphaseAuthManager
import com.kennyc.solarviewer.auth.LoginScreen
import com.kennyc.solarviewer.ui.AppTheme
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@ExperimentalComposeUiApi
class MainActivityCompose : ComponentActivity() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var authManager: EnphaseAuthManager

    private val systemsViewModel: SystemsViewModel by viewModels { factory }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = applicationContext as SolarApp
        app.component.inject(this)

        super.onCreate(savedInstanceState)

        disposables.add(
            authManager.refreshAuthState()
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .subscribe({}, {})
        )

        handleAuthIntent(intent)

        setContent {
            AppTheme {
                val authState by authManager.state
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeAsState(initial = AuthState.Unknown)

                when (val s = authState) {
                    AuthState.Authorized -> MainScreen(factory, systemsViewModel)
                    is AuthState.Error -> LoginScreen(errorMessage = s.message) {
                        authManager.startAuthorize(this)
                    }
                    AuthState.Unauthorized,
                    AuthState.Unknown -> LoginScreen { authManager.startAuthorize(this) }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun handleAuthIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (!authManager.isCallback(data)) return
        disposables.add(
            authManager.handleCallback(data)
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .subscribe({}, {})
        )
    }
}
