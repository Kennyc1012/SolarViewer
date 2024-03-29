package com.kennyc.solarviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.kennyc.solarviewer.R
import com.kennyc.solarviewer.data.model.exception.RateLimitException
import java.text.SimpleDateFormat
import java.util.*

val timeFormatter = SimpleDateFormat("h:mma", Locale.getDefault()).apply {
    timeZone = TimeZone.getDefault()
}

val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getDefault()
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Preview(showSystemUi = true)
@Composable
fun Error(
    error: Throwable? = null,
    retryClick: () -> Unit = {}
) {
    val errorText = when (error) {
        is RateLimitException -> stringResource(id = R.string.rate_limit_error)
        else -> stringResource(id = R.string.date_error)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )
        Button(onClick = retryClick) {
            Text(text = stringResource(id = R.string.error_retry))
        }
    }

}

@Composable
fun RefreshLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onRefresh: () -> Unit = {}
) {
    val currentRefresh by rememberUpdatedState(onRefresh)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentRefresh.invoke()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
//endregion