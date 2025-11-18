package com.example.rogueai.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

        if (actions.isEmpty()) {
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

        // On coupe les boutons en lignes de 4
        val chunkSize = 4
        actions.chunked(chunkSize).forEach { chunk ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunk.forEach { value ->
                    val isSelected = value == actualStatus

                    if (isSelected) {
                        // Bouton "actif" (désactivé + label explicite)
                        Button(
                            onClick = { /* rien, déjà la valeur actuelle */ },
                            enabled = false,
                            modifier = Modifier
                                .weight(1f, fill = true)
                        ) {
                            Text("$value (actuel)")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onExecuteAction(commandId, value) },
                            modifier = Modifier
                                .weight(1f, fill = true)
                        ) {
                            Text(value)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
