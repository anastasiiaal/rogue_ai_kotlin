package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rogueai.ui.theme.RogueChoiceButton

@Composable
fun SliderCommandView(
    commandId: String,
    name: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val titlePrefix = if (isHighlighted) "👀 " else ""

    Column {
        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (actions.isEmpty() && actualStatus.isBlank()) {
            Text(
                text = "Aucune valeur possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        val allValues = (actions + actualStatus)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: 0 }

        val clickableSet = actions.toSet()

        // 5 / 6 par ligne
        val columnsPerRow = if (allValues.size >= 8) 6 else 5

        val buttonHeight = 32.dp

        allValues.chunked(columnsPerRow).forEach { rowValues ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowValues.forEach { value ->
                    val selected = value == actualStatus
                    val clickable = clickableSet.contains(value)

                    RogueChoiceButton(
                        text = value,
                        selected = selected,
                        enabled = clickable && !selected, // selected = non clickable
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight),
                        onClick = { onExecuteAction(commandId, value) }
                    )
                }

                // keep columns aligned even if last row is not full
                repeat(columnsPerRow - rowValues.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

