package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Commande de type "slider".
 *
 * Exemple de payload :
 * {
 *   id: "volume_hallucinations",
 *   name: "Volume hallucinations",
 *   type: "slider",
 *   styleType: "slider",
 *   actual_status: "0",
 *   action_possible: ["1","2",...,"10"]
 * }
 */
@Composable
fun SliderCommandView(
    commandId: String,
    name: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val titlePrefix = if (isHighlighted) ">>> " else ""

    Column {
        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Type: slider – Valeur actuelle: $actualStatus",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (actions.isEmpty() && actualStatus.isBlank()) {
            Text(
                text = "Aucune valeur possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        Text(
            text = "Choisis une valeur :",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Liste stable des valeurs à afficher
        val allValues = (actions + actualStatus)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: 0 }

        val clickableSet = actions.toSet()
        val chunkSize = 2

        allValues.chunked(chunkSize).forEach { chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunk.forEach { value ->
                    val isSelected = value == actualStatus
                    val isClickable = clickableSet.contains(value)

                    val baseModifier = Modifier
                        .weight(1f, fill = true)
                        .height(44.dp) // un peu plus haut pour du confort

                    when {
                        // Valeur actuelle : bouton plein, désactivé
                        isSelected -> {
                            Button(
                                onClick = { /* rien */ },
                                enabled = false,
                                shape = RoundedCornerShape(4.dp),
                                modifier = baseModifier
                            ) {
                                Text(value)
                            }
                        }

                        // Valeur cliquable
                        isClickable -> {
                            OutlinedButton(
                                onClick = { onExecuteAction(commandId, value) },
                                shape = RoundedCornerShape(4.dp),
                                modifier = baseModifier
                            ) {
                                Text(value)
                            }
                        }

                        // Valeur affichée mais non cliquable (sécurité)
                        else -> {
                            OutlinedButton(
                                onClick = { /* non autorisé */ },
                                enabled = false,
                                shape = RoundedCornerShape(4.dp),
                                modifier = baseModifier
                            ) {
                                Text(value)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

