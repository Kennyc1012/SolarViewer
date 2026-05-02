package com.kennyc.solarviewer.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    errorMessage: String? = null,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SolarViewer",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
            text = "Sign in with your Enphase account to continue.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onSignInClick) {
            Text("Sign in with Enphase")
        }
        if (!errorMessage.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
