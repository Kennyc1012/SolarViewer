package com.kennyc.solarviewer.data

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    data object Unknown : AuthState()
    data object Unauthorized : AuthState()
    data object Authorized : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthManager {

    val state: StateFlow<AuthState>

    suspend fun refreshAuthState()

    suspend fun buildAuthorizeUri(): Uri

    fun isCallback(uri: Uri?): Boolean

    suspend fun handleCallback(uri: Uri)

    suspend fun signOut()
}
