package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.rogueai.ui.theme.RoguePalette

@Composable
fun ThreatBar(
    threatLevel: Int?,
    modifier: Modifier = Modifier
) {
    // If threat level is null, do not display the bar
    threatLevel?.let { level ->
        // Colors based on threat level
        val barColor = when {
            level <= 20 -> RoguePalette.ThreatVibrantGreen
            level <= 40 -> RoguePalette.ThreatLightGreen
            level <= 60 -> RoguePalette.ThreatYellow
            level <= 80 -> RoguePalette.ThreatOrange
            else -> RoguePalette.ThreatRed
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Niveau de Menace",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$level / 100",
                    style = MaterialTheme.typography.labelLarge,
                    color = barColor
                )
            }

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { level / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.2f)
            )
        }
    }
}