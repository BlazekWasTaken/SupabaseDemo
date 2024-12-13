package com.example.supabasedemo.compose.views

import UwbManagerSingleton
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.ui.theme.AppTheme
import java.util.Locale

@Composable
fun UwbDataView() {
    val distance by UwbManagerSingleton.distance.collectAsState(initial = -1.0)
    val azimuth by UwbManagerSingleton.azimuth.collectAsState(initial = -1.0)

    Box(
        modifier = Modifier.border(1.dp, AppTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Log.e("uwb", "distance $distance angle $azimuth")
            Text(text = "UWB")
            Text(text = "distance: ${distance.fixDistanceForScreen()}")
            Text(text = "angle: ${azimuth.fixAngleForScreen()}")
        }
    }
}

private fun Double.fixDistanceForScreen(): String {
    return if (this != -1.0) {
        String.format(Locale.getDefault(), "%.2f", this) + "m"
    } else {
        "Loading..."
    }
}

private fun Double.fixAngleForScreen(): String {
    return if (this != -1.0) {
        String.format(Locale.getDefault(), "%.0f", this) + "\u00B0"
    } else {
        "Loading..."
    }
}