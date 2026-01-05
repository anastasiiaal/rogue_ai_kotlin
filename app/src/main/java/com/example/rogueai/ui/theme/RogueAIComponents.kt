package com.example.rogueai.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dark blue background gradient used throughout the app
 */
fun Modifier.rogueBackground(): Modifier =
    this.background(
        Brush.verticalGradient(
            colors = listOf(
                RoguePalette.BackgroundTop,
                RoguePalette.BackgroundBottom
            )
        )
    )

/**
 * Card used as main container on most screens
 */
@Composable
fun RogueMainCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = RoguePalette.CardBlue.copy(alpha = 0.95f),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            content()
        }
    }
}

/**
 * RogueAi title chip
 */
@Composable
fun RogueChipTitle(text: String = "ROGUE AI") {
    Surface(
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = androidx.compose.ui.graphics.Color(0xFFFFF176),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Main button (e.g. : "Démarrer une partie").
 */
@Composable
fun RoguePrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RoguePalette.ButtonYellow,
            contentColor = RoguePalette.ButtonYellowText
        )
    ) {
        if (leadingContent != null) {
            leadingContent()
        }
        Text(text)
    }
}

/**
 * Secondary button (e.g. : "Rejoindre une partie").
 */
@Composable
fun RogueSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RoguePalette.ButtonSalmon,
            contentColor = RoguePalette.ButtonSalmonText
        )
    ) {
        Text(text)
    }
}

/**
 * Accent button (e.g. : "Rejoindre une partie").
 */
@Composable
fun RogueAccentButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RoguePalette.ButtonJoinBlue,
            contentColor = RoguePalette.ButtonJoinText
        )
    ) {
        Text(text)
    }
}

/**
 * Choice button used in command views
 */
@Composable
fun RogueChoiceButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    val colors = ButtonDefaults.buttonColors(
        containerColor = when {
            selected -> RoguePalette.ButtonYellow
            else -> RoguePalette.CardBlue
        },
        contentColor = when {
            selected -> RoguePalette.ButtonYellowText
            else -> androidx.compose.ui.graphics.Color.White
        },
        disabledContainerColor = RoguePalette.ButtonSalmon,
        disabledContentColor = RoguePalette.CardBlue
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = shape,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
