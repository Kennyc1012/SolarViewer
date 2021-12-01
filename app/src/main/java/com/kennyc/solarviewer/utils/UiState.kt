package com.kennyc.solarviewer.utils

sealed class UiState

object LoadingState : UiState()

data class ContentState<T:Any>(val item: T) : UiState()

data class ErrorState(val error: Throwable) : UiState()