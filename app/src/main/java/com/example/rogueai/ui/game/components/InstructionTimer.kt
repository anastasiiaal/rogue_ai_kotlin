package com.example.rogueai.ui.game.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.rogueai.ui.theme.RoguePalette
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun InstructionTimer(
    durationMs: Long?,
    instructionKey: String,
    modifier: Modifier = Modifier
) {
    if (durationMs == null || durationMs <= 0L) return

    var timeLeft by remember(instructionKey) { mutableLongStateOf(durationMs) }

    LaunchedEffect(instructionKey) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMs

        while (timeLeft > 0) {
            val currentTime = System.currentTimeMillis()
            timeLeft = (endTime - currentTime).coerceAtLeast(0L)
            delay(16)
        }
    }

    val seconds = timeLeft / 1000
    val millis = timeLeft % 1000
    val formattedTime = String.format(Locale.US, "%d.%03d", seconds, millis)

    // --- Logical colour ---
    // Get the default content color from the theme
    val defaultColor = LocalContentColor.current
    // Change color (to red) if less than 3 seconds
    val timerColor = if (timeLeft < 3000) RoguePalette.ThreatRed else defaultColor

    Text(
        text = formattedTime,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        ),
        color = timerColor,
        modifier = modifier
    )
}