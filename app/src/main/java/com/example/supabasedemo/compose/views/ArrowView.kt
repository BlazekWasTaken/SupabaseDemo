package com.example.supabasedemo.compose.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.R
import com.example.supabasedemo.ui.theme.AppTheme

@Composable
fun ArrowView(
    getAz: () -> Double
) {
    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.colorScheme.outline)
            .size(150.dp, 150.dp),

    ) {
        Image(
            painter = painterResource(R.drawable.arrow),
            "Arrow image",
            Modifier
                .rotate(getAz().toFloat())
                .fillMaxSize()
                .padding(25.dp)
        )
    }
}